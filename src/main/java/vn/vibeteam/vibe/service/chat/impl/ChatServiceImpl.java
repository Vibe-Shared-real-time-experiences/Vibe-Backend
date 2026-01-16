package vn.vibeteam.vibe.service.chat.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import vn.vibeteam.vibe.common.EventType;
import vn.vibeteam.vibe.common.MessageStatus;
import vn.vibeteam.vibe.dto.common.CursorResponse;
import vn.vibeteam.vibe.dto.request.chat.CreateMessageRequest;
import vn.vibeteam.vibe.dto.response.chat.*;
import vn.vibeteam.vibe.dto.websocket.WsAttachmentResponse;
import vn.vibeteam.vibe.dto.websocket.WsEvent;
import vn.vibeteam.vibe.dto.websocket.WsMessageResponse;
import vn.vibeteam.vibe.dto.websocket.WsUserSummary;
import vn.vibeteam.vibe.exception.AppException;
import vn.vibeteam.vibe.exception.ErrorCode;
import vn.vibeteam.vibe.model.server.Channel;
import vn.vibeteam.vibe.model.server.ChannelMessage;
import vn.vibeteam.vibe.model.server.MessageAttachment;
import vn.vibeteam.vibe.model.server.ServerMember;
import vn.vibeteam.vibe.repository.chat.ChannelRepository;
import vn.vibeteam.vibe.repository.chat.MessageRepository;
import vn.vibeteam.vibe.repository.chat.ServerRepository;
import vn.vibeteam.vibe.service.chat.ChatService;
import vn.vibeteam.vibe.util.SecurityUtils;
import vn.vibeteam.vibe.util.SnowflakeIdGenerator;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ServerRepository serverRepository;
    private final ChannelRepository channelRepository;
    private final MessageRepository messageRepository;

    private final SnowflakeIdGenerator idGenerator;
    private final SimpMessagingTemplate messagingTemplate;
    private static final String WEBSOCKET_SERVER_DESTINATION_PREFIX = "/topic/servers/";
    private static final String WEBSOCKET_CHANNEL_DESTINATION_PREFIX = "/topic/channels/";
    private static final String ATTACHMENT_BASE_URL = "https://vibe-attachments.s3.amazonaws.com/";

    private final SecurityUtils securityUtils;

    @Override
    public CreateMessageResponse sendMessage(String channelId, CreateMessageRequest request) {
        log.info("Sending message to channelId: {}", channelId);

        // 1. Find channel
        Channel channel = channelRepository.findById(Long.valueOf(channelId))
                                           .orElseThrow(() -> new AppException(ErrorCode.CHANNEL_NOT_FOUND));

        Long currentUserId = securityUtils.getCurrentUserId();

        ServerMember member = serverRepository.findMemberById(
                channel.getServer().getId(),
                currentUserId
        ).orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_IN_SERVER));

        // 2. Create ChannelMessage
        Long messageId = generateSnowflakeId();
        ChannelMessage.ChannelMessageBuilder channelMessageBuilder =
                ChannelMessage.builder()
                              .id(messageId)
                              .channel(channel)
                              .author(member)
                              .content(request.getContent());

        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            String url = ATTACHMENT_BASE_URL + messageId + "/";

            List<MessageAttachment> attachments = request.getAttachments().stream().map(
                    att -> MessageAttachment.builder()
                                            .url(url)
                                            .type(att.getType())
                                            .contentType(att.getContentType())
                                            .width(att.getWidth())
                                            .height(att.getHeight())
                                            .size(att.getSize())
                                            .build()
            ).toList();

            channelMessageBuilder.attachmentMetadata(attachments);
        }

        // 3. Persist message
        ChannelMessage channelMessages = channelMessageBuilder.build();
        channelMessages = messageRepository.save(channelMessages);

        // 4. Broadcast message (server + channel)
        WsEvent<WsMessageResponse> wsChannelEvent = createWsChannelEvent(channelMessages);

        // TODO: Remove this line after testing
        WsEvent<String> wsServerEvent = WsEvent.<String>builder()
                                               .eventType(EventType.MESSAGE_CREATED)
                                               .data("New message in server " + channel.getServer().getId())
                                               .build();

        messagingTemplate.convertAndSend(WEBSOCKET_SERVER_DESTINATION_PREFIX + channel.getServer().getId(),
                                         wsServerEvent);
        messagingTemplate.convertAndSend(WEBSOCKET_CHANNEL_DESTINATION_PREFIX + channel.getId(), wsChannelEvent);

        // 5. Return response
        log.info("Message sent successfully with id: {}", channelMessages.getId());
        return mapToCreateMessageResponse(channelMessages, request.getKey());
    }

    @Override
    public CursorResponse<ChannelHistoryResponse> getChannelMessages(String channelId, String cursor, int limit) {
        log.info("Fetching messages for channelId: {}, cursor: {}, limit: {}", channelId, cursor, limit);

        // 1. Fetch messages
        Pageable pageable = PageRequest.of(
                0,      // Using cursor pagination, so page number is always 0
                limit + 1,          // Fetch one extra to determine if there's a next page
                Sort.by(Sort.Direction.DESC, "id")
        );

        List<ChannelMessage> messages;
        if (cursor != null) {
            Long cursorId = Long.valueOf(cursor);
            messages = messageRepository.findOlderMessagesById(Long.parseLong(channelId), cursorId, pageable);
        } else {
            messages = messageRepository.findLatestMessages(Long.parseLong(channelId), pageable);
        }

        // 2. Create response
        ChannelHistoryResponse channelHistoryResponse = mapToChannelHistoryResponse(messages);

        boolean hasNext = messages.size() > limit;
        if (hasNext) {
            messages = messages.subList(0, limit);
        }

        // 3. Determine next cursor
        String nextCursor = null;
        if (hasNext) {
            Long lastMessageId = messages.get(messages.size() - 1).getId();
            nextCursor = lastMessageId.toString();
        }

        log.info("Fetched {} messages for channelId: {}", channelHistoryResponse.getMessages().size(), channelId);
        return CursorResponse.<ChannelHistoryResponse>builder()
                             .items(channelHistoryResponse)
                             .nextCursor(nextCursor)
                             .hasMore(hasNext)
                             .build();
    }

    @Override
    @Transactional
    public void editMessageContent(String messageId, String newContent) {
        log.info("Editing content of messageId: {}", messageId);

        // 1. Find message
        ChannelMessage channelMessage = messageRepository.findById(Long.valueOf(messageId))
                                                         .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));

        // 2. Verify author
        boolean isOwner = isAuthor(channelMessage.getAuthor().getId());
        if (!isOwner) {
            log.error("User {} is trying to edit message {} without ownership",
                      securityUtils.getCurrentUserId(), messageId);
            throw new AppException(ErrorCode.UNAUTHORIZED_ACTION);
        }

        // 3. Update content
        channelMessage.setContent(newContent);
        messageRepository.save(channelMessage);
        log.info("Message {} content updated successfully", messageId);
    }

    @Override
    @Transactional
    public void deleteMessage(String messageId) {
        log.info("Deleting messageId: {}", messageId);

        // 1. Find message
        ChannelMessage channelMessage = messageRepository.findById(Long.valueOf(messageId))
                                                         .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));

        // 2. Verify author
        boolean isOwner = isAuthor(channelMessage.getAuthor().getId());
        boolean isServerAdmin = isServerAdmin(channelMessage.getChannel().getServer().getId());
        if (!isOwner && !isServerAdmin) {
            log.error("User {} is trying to delete message {} without ownership",
                      securityUtils.getCurrentUserId(), messageId);
            throw new AppException(ErrorCode.UNAUTHORIZED_ACTION);
        }

        // 3. Delete message
        messageRepository.deleteMessage(Long.valueOf(messageId));
        log.info("Message {} deleted successfully", messageId);
    }

    private boolean isAuthor(Long ownerId) {
        Long currentUserId = securityUtils.getCurrentUserId();
        return ownerId.equals(currentUserId);
    }

    private boolean isServerAdmin(Long adminId) {
        Long currentUserId = securityUtils.getCurrentUserId();
        return adminId.equals(currentUserId);
    }

    private ChannelHistoryResponse mapToChannelHistoryResponse(List<ChannelMessage> messages) {
        List<MessageResponse> messageResponses = messages.stream().map(
                msg -> MessageResponse.builder()
                                      .id(msg.getId())
                                      .senderId(msg.getAuthor().getId())
                                      .channelId(msg.getChannel().getId())
                                      .content(msg.getContent())
                                      .attachments(
                                              msg.getAttachmentMetadata() != null ?
                                                      msg.getAttachmentMetadata().stream().map(
                                                              att -> MessageAttachmentResponse.builder()
                                                                                              .url(att.getUrl())
                                                                                              .type(att.getType())
                                                                                              .contentType(
                                                                                                      att.getContentType())
                                                                                              .width(att.getWidth())
                                                                                              .height(att.getHeight())
                                                                                              .size(att.getSize())
                                                                                              .build()
                                                      ).toList() : null
                                      )
                                      .build()
        ).toList();

        Set<MemberSummaryInfoResponse> memberInfos = messages.stream().map(
                msg -> MemberSummaryInfoResponse.builder()
                                                .memberId(msg.getAuthor().getId())
                                                .displayName(msg.getAuthor().getNickname())
                                                .avatarUrl(msg.getAuthor().getAvatarUrl())
                                                .build()
        ).collect(Collectors.toSet());

        return ChannelHistoryResponse.builder()
                                     .messages(messageResponses)
                                     .memberInfos(memberInfos)
                                     .build();
    }

    private WsEvent<WsMessageResponse> createWsChannelEvent(ChannelMessage channelMessage) {
        WsMessageResponse.WsMessageResponseBuilder wsMessageResponseBuilder =
                WsMessageResponse.builder()
                                 .id(channelMessage.getId())
                                 .content(channelMessage.getContent())
                                 .channelId(channelMessage.getChannel().getId())
                                 .author(WsUserSummary.builder()
                                                      .id(channelMessage.getAuthor().getId())
                                                      .username(channelMessage.getAuthor().getNickname())
                                                      .avatarUrl(channelMessage.getAuthor().getAvatarUrl())
                                                      .build())
                                 .createdAt(channelMessage.getCreatedAt().toString());
        if (channelMessage.getAttachmentMetadata() != null && !channelMessage.getAttachmentMetadata()
                                                                             .isEmpty()) {
            wsMessageResponseBuilder.attachments(channelMessage.getAttachmentMetadata().stream().map(
                    attachment -> WsAttachmentResponse.builder()
                                                      .url(attachment.getUrl())
                                                      .type(attachment.getType())
                                                      .contentType(attachment.getContentType())
                                                      .width(attachment.getWidth())
                                                      .height(attachment.getHeight())
                                                      .size(attachment.getSize())
                                                      .build()).toList());
        }

        WsMessageResponse wsMessageResponse = wsMessageResponseBuilder.build();

        return WsEvent.<WsMessageResponse>builder()
                      .eventType(EventType.MESSAGE_CREATED)
                      .data(wsMessageResponse)
                      .build();
    }

    private Long generateSnowflakeId() {
        return idGenerator.nextId();
    }

    private CreateMessageResponse mapToCreateMessageResponse(ChannelMessage channelMessage, String key) {
        return CreateMessageResponse.builder()
                                    .messageId(channelMessage.getId().toString())
                                    .key(key)
                                    .status(MessageStatus.SUCCESS)
                                    .build();
    }
}
