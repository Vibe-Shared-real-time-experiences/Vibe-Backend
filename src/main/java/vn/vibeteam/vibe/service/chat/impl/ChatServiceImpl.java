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
import vn.vibeteam.vibe.model.authorization.UserProfile;
import vn.vibeteam.vibe.model.server.Channel;
import vn.vibeteam.vibe.model.server.ChannelMessage;
import vn.vibeteam.vibe.model.server.MessageAttachment;
import vn.vibeteam.vibe.model.server.ServerMember;
import vn.vibeteam.vibe.repository.chat.ChannelRepository;
import vn.vibeteam.vibe.repository.chat.MessageRepository;
import vn.vibeteam.vibe.repository.chat.ServerRepository;
import vn.vibeteam.vibe.repository.user.UserProfileRepository;
import vn.vibeteam.vibe.service.chat.ChatService;
import vn.vibeteam.vibe.util.SnowflakeIdGenerator;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ServerRepository serverRepository;
    private final ChannelRepository channelRepository;
    private final MessageRepository messageRepository;
    private final UserProfileRepository userProfileRepository;

    private final SnowflakeIdGenerator idGenerator;
    private final SimpMessagingTemplate messagingTemplate;
    private static final String WEBSOCKET_SERVER_DESTINATION_PREFIX = "/topic/servers/";
    private static final String WEBSOCKET_CHANNEL_DESTINATION_PREFIX = "/topic/channels/";
    private static final String ATTACHMENT_BASE_URL = "https://vibe-attachments.s3.amazonaws.com/";

    @Override
    public CreateMessageResponse sendMessage(Long userId, Long channelId, CreateMessageRequest request) {
        log.info("Sending message to channelId: {}", channelId);

        // 1. Find channel
        Channel channel = channelRepository.findById(channelId)
                                           .orElseThrow(() -> new AppException(ErrorCode.CHANNEL_NOT_FOUND));

        ServerMember member = serverRepository.findMemberByServerIdAndUserId(
                channel.getServer().getId(),
                userId
        ).orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_IN_SERVER));

        // 2. Create ChannelMessage
        Long messageId = generateSnowflakeId();
        ChannelMessage.ChannelMessageBuilder channelMessageBuilder =
                ChannelMessage.builder()
                              .id(messageId)
                              .channel(channel)
                              .author(member)
                              .clientUniqueId(request.getClientUniqueId())
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
        return mapToCreateMessageResponse(channelMessages, request.getClientUniqueId());
    }

    @Override
    public CursorResponse<ChannelHistoryResponse> getChannelMessages(Long channelId, Long cursor, int limit) {
        log.info("Fetching messages for channelId: {}, cursor: {}, limit: {}", channelId, cursor, limit);

        // 1. Fetch messages
        Pageable pageable = PageRequest.of(
                0,      // Using cursor pagination, so page number is always 0
                limit + 1,          // Fetch one extra to determine if there's a next page
                Sort.by(Sort.Direction.DESC, "id")
        );

        List<ChannelMessage> messages;
        if (cursor != null) {
            messages = messageRepository.findOlderMessagesById(channelId, cursor, pageable);
        } else {
            messages = messageRepository.findLatestMessages(channelId, pageable);
        }

        // 2. Create response
        List<UserProfile> userProfiles = userProfileRepository.findAllById(
                messages.stream()
                        .map(msg -> msg.getAuthor().getUser().getId())
                        .collect(Collectors.toSet())
        );
        ChannelHistoryResponse channelHistoryResponse = mapToChannelHistoryResponse(messages, userProfiles);

        boolean hasNext = messages.size() > limit;
        if (hasNext) {
            messages = messages.subList(0, limit);
        }

        // 3. Determine next cursor
        Long nextCursor = null;
        if (hasNext) {
            nextCursor = messages.getLast().getId();
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
    public void editMessageContent(Long userId, Long messageId, String newContent) {
        log.info("Editing content of messageId: {}", messageId);

        // 1. Find message
        ChannelMessage channelMessage = messageRepository.findById(messageId)
                                                         .orElseThrow(
                                                                 () -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));

        // 2. Verify author
        boolean isOwner = isAuthor(userId, channelMessage.getAuthor().getId());
        if (!isOwner) {
            log.error("User {} is trying to edit message {} without ownership",
                      userId, messageId);
            throw new AppException(ErrorCode.UNAUTHORIZED_ACTION);
        }

        // 3. Update content
        channelMessage.setContent(newContent);
        messageRepository.save(channelMessage);
        log.info("Message {} content updated successfully", messageId);
    }

    @Override
    @Transactional
    public void deleteMessage(Long userId, Long messageId) {
        log.info("Deleting messageId: {}", messageId);

        // 1. Find message
        ChannelMessage channelMessage = messageRepository.findById(messageId)
                                                         .orElseThrow(
                                                                 () -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));

        // 2. Verify author
        boolean isOwner = isAuthor(userId, channelMessage.getAuthor().getId());
        boolean isServerAdmin = Objects.equals(userId, channelMessage.getChannel().getServer().getId());
        if (!isOwner && !isServerAdmin) {
            log.error("User {} is trying to delete message {} without ownership",
                      userId, messageId);
            throw new AppException(ErrorCode.UNAUTHORIZED_ACTION);
        }

        // 3. Delete message
        messageRepository.deleteMessage(messageId);
        log.info("Message {} deleted successfully", messageId);
    }

    private boolean isAuthor(Long userId, Long authorId) {
        return authorId.equals(userId);
    }

    private ChannelHistoryResponse mapToChannelHistoryResponse(List<ChannelMessage> messages,
                                                               List<UserProfile> userProfiles) {
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

        Set<MemberSummaryInfoResponse> memberInfos = userProfiles.stream().map(
                profile -> MemberSummaryInfoResponse.builder()
                                                    .memberId(profile.getUser().getId())
                                                    .displayName(profile.getDisplayName())
                                                    .avatarUrl(profile.getAvatarUrl())
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
                                                      .avatarUrl(channelMessage.getAuthor()
                                                                               .getUser()
                                                                               .getUserProfile()
                                                                               .getAvatarUrl())
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

    private long generateSnowflakeId() {
        return idGenerator.nextId();
    }

    private CreateMessageResponse mapToCreateMessageResponse(ChannelMessage channelMessage, String key) {
        return CreateMessageResponse.builder()
                                    .messageId(channelMessage.getId().toString())
                                    .clientUniqueId(key)
                                    .status(MessageStatus.SUCCESS)
                                    .build();
    }
}
