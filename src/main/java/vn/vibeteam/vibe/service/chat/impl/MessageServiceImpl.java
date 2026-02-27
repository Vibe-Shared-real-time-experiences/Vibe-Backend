package vn.vibeteam.vibe.service.chat.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import vn.vibeteam.vibe.common.FetchDirection;
import vn.vibeteam.vibe.common.MessageStatus;
import vn.vibeteam.vibe.dto.common.CursorResponse;
import vn.vibeteam.vibe.dto.event.ChannelMessageCreatedEvent;
import vn.vibeteam.vibe.dto.request.chat.CreateMessageRequest;
import vn.vibeteam.vibe.dto.response.chat.*;
import vn.vibeteam.vibe.dto.websocket.MessageBroadcastEvent;
import vn.vibeteam.vibe.exception.AppException;
import vn.vibeteam.vibe.exception.ErrorCode;
import vn.vibeteam.vibe.model.channel.Channel;
import vn.vibeteam.vibe.model.channel.ChannelMessage;
import vn.vibeteam.vibe.model.channel.MessageAttachment;
import vn.vibeteam.vibe.model.server.Category;
import vn.vibeteam.vibe.model.server.Server;
import vn.vibeteam.vibe.model.server.ServerMember;
import vn.vibeteam.vibe.repository.chat.ChannelRepository;
import vn.vibeteam.vibe.repository.chat.MessageRepository;
import vn.vibeteam.vibe.repository.chat.ServerRepository;
import vn.vibeteam.vibe.repository.chat.cache.ChannelCacheRepository;
import vn.vibeteam.vibe.repository.chat.cache.MessageCacheRepository;
import vn.vibeteam.vibe.repository.chat.cache.ServerCacheRepository;
import vn.vibeteam.vibe.repository.chat.stream.MessageStreamProducer;
import vn.vibeteam.vibe.service.chat.MessageService;
import vn.vibeteam.vibe.util.SnowflakeIdGenerator;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageServiceImpl implements MessageService {

    private final ServerRepository serverRepository;
    private final ChannelRepository channelRepository;
    private final MessageRepository messageRepository;

    private final ServerCacheRepository serverCacheRepository;
    private final ChannelCacheRepository channelCacheRepository;
    private final MessageCacheRepository messageCacheRepository;

    private final MessageStreamProducer messageStreamProducer;

    private final SnowflakeIdGenerator idGenerator;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    private final ApplicationEventPublisher eventPublisher;
    private static final String ATTACHMENT_BASE_URL = "https://vibe-attachments.s3.amazonaws.com/";

    @Override
    public CreateMessageResponse sendMessage(Long userId, Long channelId, CreateMessageRequest request) {
        log.info("Sending message to channelId: {}", channelId);

        // 1. Find channel
        Channel channel = findChannelReferenceById(channelId);
        ServerMember member = findServerMemberReference(channel.getServer().getId(), userId);

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

        // 3. Persist message (cache)
        ChannelMessage channelMessage = channelMessageBuilder.build();
        channelMessage.setCreatedAt(java.time.LocalDateTime.now());
        channelMessage.setUpdatedAt(java.time.LocalDateTime.now());

        messageCacheRepository.saveMessage(channelId, mapToMessageResponse(channelMessage));

        // 4. Send to stream for async DB persistence
        messageStreamProducer.sendToStream(createMessageCreatedEvent(channelMessage));

        // 5. Broadcast message (server + channel)
        eventPublisher.publishEvent(new MessageBroadcastEvent(this, channelMessage, channel.getServer().getId()));

        // 5. Return response
        log.info("Message sent successfully with id: {}", channelMessage.getId());
        return mapToCreateMessageResponse(channelMessage, request.getClientUniqueId());
    }

    private Channel findChannelReferenceById(Long channelId) {
        // 1. Try to get channel from cache
        ChannelResponse channelById = channelCacheRepository.getChannelById(channelId);
        if (channelById != null) {
            log.info("Channel {} retrieved from cache", channelId);
            return Channel.builder()
                          .id(channelById.getId())
                          .name(channelById.getName())
                          .server(Server.builder().id(channelById.getServerId()).build())
                          .category(Category.builder().id(channelById.getCategoryId()).build())
                          .position(channelById.getPosition())
                          .type(channelById.getType())
                          .build();
        }

        // 2. Cache miss - get channel from DB
        Channel channel = channelRepository.findById(channelId)
                                           .orElseThrow(() -> new AppException(ErrorCode.CHANNEL_NOT_FOUND));

        // 3. Add to cache
        channelCacheRepository.saveChannel(mapToChannelResponse(channel));
        return channel;
    }

    private ServerMember findServerMemberReference(Long serverId, Long userId) {
        // 1. Try to get server member from cache
        Long serverMemberId = serverCacheRepository.getServerMemberId(serverId, userId);
        if (serverMemberId != null) {
            log.info("Server member {} retrieved from cache for serverId: {}, userId: {}",
                     serverMemberId, serverId, userId);
            return ServerMember.builder()
                               .id(serverMemberId)
                               .build();
        }

        // 2. Cache miss - get server member from DB
        ServerMember member = serverRepository.findMemberByServerIdAndUserId(
                serverId,
                userId
        ).orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_IN_SERVER));

        // 3. Add to cache
        serverCacheRepository.saveServerMemberId(serverId, member.getId());

        return member;
    }

    @Override
    public CursorResponse<ChannelHistoryResponse> getChannelMessages(Long channelId, Long cursor,
                                                                     FetchDirection direction, int limit) {
        log.info("Fetching messages for channelId: {}, cursor: {}, limit: {}", channelId, cursor, limit);

        // 1. Fetch messages from cache and return if found
        List<MessageResponse> message = messageCacheRepository.getMessages(channelId, cursor, direction, limit + 1);
        // TODO: This solution away cache miss on any channel have less than 'limit' message
        if (message.size() - limit > 0) {
            log.info("CACHE HIT! Messages retrieved from cache for channelId: {}, cursor: {}, limit: {}", channelId, cursor,
                     limit);
            return createCursorResponse(mapToChannelHistoryResponse(message), limit, direction);
        }

        // 2. Fetch messages from DB
        List<ChannelMessage> messages =
                getChannelMessagesFromDatabase(channelId, cursor, direction, limit + 1);

        List<MessageResponse> messageResponses = mapToMessageResponses(messages);
        ChannelHistoryResponse channelHistoryResponse = mapToChannelHistoryResponse(messageResponses);
        CursorResponse<ChannelHistoryResponse> cursorResponse =
                createCursorResponse(channelHistoryResponse, limit, direction);

        // 4. Save fetched messages to cache
        messageCacheRepository.saveMessages(channelId, messageResponses);

        log.info("Messages retrieved from DB for channelId: {}, cursor: {}, limit: {}", channelId, cursor, limit);
        return cursorResponse;
    }

    // TODO: This only for testing cache with String type
//    @Override
//    public CursorResponse<ChannelHistoryResponse> getChannelMessages(Long channelId, Long cursor,
//                                                                     FetchDirection direction, int limit) {
//        log.info("Fetching messages for channelId: {}, cursor: {}, limit: {}", channelId, cursor, limit);
//
//        // 1. Fetch messages from cache and return if found
//        ChannelHistoryResponse channelHistoryResponse =
//                messageCacheRepository.getMessages(channelId, cursor, limit);
//        if (channelHistoryResponse.getMessages() != null) {
//            log.info("Messages retrieved from cache for channelId: {}, cursor: {}, limit: {}", channelId, cursor,
//                     limit);
//            return createCursorResponse(channelHistoryResponse);
//        }
//
//        // 2. Fetch messages from DB
//        List<ChannelMessage> messages =
//                getChannelMessagesFromDatabase(channelId, cursor, direction, limit);
//
//        // 3. Save fetched messages to cache
//        List<MessageResponse> messageResponses = mapToMessageResponses(messages);
//        messageCacheRepository.saveMessages(channelId, messageResponses);
//
//        // 4. Return response
//        CursorResponse<ChannelHistoryResponse> cursorResponse =
//                createCursorResponse(ChannelHistoryResponse.builder().messages(messageResponses).build(),
//                                     limit,
//                                     direction);
//
//
//        log.info("Messages retrieved from DB for channelId: {}, cursor: {}, limit: {}", channelId, cursor, limit);
//        return cursorResponse;
//    }

    private List<ChannelMessage> getChannelMessagesFromDatabase(Long channelId, Long cursor, FetchDirection direction,
                                                                int limit) {
        Pageable pageable = PageRequest.of(
                0,      // Using cursor pagination, so page number is always 0
                limit
        );

        List<ChannelMessage> messages;
        if (cursor == null) {
            messages = messageRepository.findLatestMessages(channelId, pageable);
        } else {
            if (direction == FetchDirection.BEFORE) {
                messages = messageRepository.findOlderMessagesById(channelId, cursor, pageable);
            } else { // direction == AFTER
                messages = messageRepository.findNewerMessagesById(channelId, cursor, pageable);
            }
        }
        return messages;
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

    @Transactional(rollbackOn = Exception.class)
    public void processBatch(List<ChannelMessageCreatedEvent> messages) {
        bulkInsertMessages(messages);
        updateLastMessageIds(messages);

        log.info("Processed batch of {} messages", messages.size());
    }

    private void updateLastMessageIds(List<ChannelMessageCreatedEvent> messages) {
        Map<Long, Long> channelLastMessageIdMap = new HashMap<>();

        for (ChannelMessageCreatedEvent message : messages) {
            Long channelId = message.getChannelId();
            Long messageId = message.getMessageId();

            // Get last messageId for specific channel
            channelLastMessageIdMap.merge(channelId, messageId, Math::max);
        }

        // Update lastMessageId for each channel
        for (Map.Entry<Long, Long> entry : channelLastMessageIdMap.entrySet()) {
            Long channelId = entry.getKey();
            Long lastMessageId = entry.getValue();
            channelRepository.updateLastMessageId(channelId, lastMessageId);
        }
    }

    public void bulkInsertMessages(List<ChannelMessageCreatedEvent> payloads) {
        String sql = """
                INSERT INTO channel_messages 
                (
                    id,
                    channel_id, 
                    author_id, 
                    client_unique_id, 
                    content, 
                    attachments, 
                    meta_data, 
                    is_pinned, 
                    is_edited, 
                    created_at, 
                    updated_at, 
                    is_deleted
                ) 
                VALUES (?, ?, ?, ?, ?, ?::jsonb, ?::jsonb, ?, false, ?, ?, false)
                ON CONFLICT (client_unique_id) DO NOTHING 
                """;

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ChannelMessageCreatedEvent msg = payloads.get(i);
                String attachmentsJson = (msg.getAttachments() != null)
                        ? objectMapper.writeValueAsString(msg.getAttachments())
                        : "[]";
                String metaJson = (msg.getMetadata() != null)
                        ? objectMapper.writeValueAsString(msg.getMetadata())
                        : "{}";

                ps.setLong(1, msg.getMessageId());
                ps.setLong(2, msg.getChannelId());
                ps.setLong(3, msg.getAuthorId());
                ps.setString(4, msg.getClientUniqueId());
                ps.setString(5, msg.getContent());
                ps.setString(6, attachmentsJson);
                ps.setString(7, metaJson);
                ps.setBoolean(8, msg.getIsPinned() != null && msg.getIsPinned());
                Timestamp timestamp =
                        Timestamp.valueOf(msg.getCreatedAt() != null ? msg.getCreatedAt() : LocalDateTime.now());
                ps.setTimestamp(9, timestamp);
                ps.setTimestamp(10, timestamp);
            }

            @Override
            public int getBatchSize() {
                return payloads.size();
            }
        });
    }

    private ChannelMessageCreatedEvent createMessageCreatedEvent(ChannelMessage channelMessage) {
        return ChannelMessageCreatedEvent.builder()
                                         .channelId(channelMessage.getChannel().getId())
                                         .authorId(channelMessage.getAuthor().getId())
                                         .messageId(channelMessage.getId())
                                         .clientUniqueId(channelMessage.getClientUniqueId())
                                         .content(channelMessage.getContent())
                                         .attachments(channelMessage.getAttachmentMetadata())
                                         .metadata(channelMessage.getMetadata())
                                         .isPinned(channelMessage.getIsPinned())
                                         .createdAt(channelMessage.getCreatedAt())
                                         .build();
    }

    private ChannelResponse mapToChannelResponse(Channel channel) {
        return ChannelResponse.builder()
                              .id(channel.getId())
                              .serverId(channel.getServer().getId())
                              .categoryId(channel.getCategory().getId())
                              .name(channel.getName())
                              .position(channel.getPosition())
                              .type(channel.getType())
                              .build();
    }

    private MessageResponse mapToMessageResponse(ChannelMessage channelMessage) {
        return MessageResponse.builder()
                              .id(channelMessage.getId())
                              .authorId(channelMessage.getAuthor().getId())
                              .channelId(channelMessage.getChannel().getId())
                              .content(channelMessage.getContent())
                              .attachments(
                                      channelMessage.getAttachmentMetadata() != null ?
                                              channelMessage.getAttachmentMetadata().stream().map(
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
//                                      .metadata(channelMessage.getMetadata().getReactions().forEach())
                              .createdAt(channelMessage.getCreatedAt())
                              .updatedAt(channelMessage.getUpdatedAt())
                              .build();
    }

    private ChannelHistoryResponse mapToChannelHistoryResponse(List<MessageResponse> message) {
        return ChannelHistoryResponse.builder()
                                     .messages(message)
                                     .build();
    }

    // TODO: This only for testing cache with String type
//    private ChannelHistoryResponse mapToChannelHistoryResponse(Set<String> messageResponses) {
//        StringBuilder sb = new StringBuilder();
//        sb.append("[");
//        Iterator<String> iterator = messageResponses.iterator();
//        while (iterator.hasNext()) {
//            sb.append(iterator.next());
//            if (iterator.hasNext()) {
//                sb.append(",");
//            }
//        }
//        sb.append("]");
//
//        String messageJsonArray = sb.toString();
//
//        return ChannelHistoryResponse.builder()
//                                     .messages(messageJsonArray)
//                                     .build();
//    }

    private static List<MessageResponse> mapToMessageResponses(List<ChannelMessage> messages) {
        List<MessageResponse> messageResponses = messages.stream().map(
                msg -> MessageResponse.builder()
                                      .id(msg.getId())
                                      .authorId(msg.getAuthor().getId())
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
//                                      .metadata(msg.getMetadata().getReactions().forEach())
                                      .createdAt(msg.getCreatedAt())
                                      .updatedAt(msg.getUpdatedAt())
                                      .build()
        ).toList();
        return messageResponses;
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

    private CursorResponse<ChannelHistoryResponse> createCursorResponse(ChannelHistoryResponse channelHistoryResponse,
                                                                        int limit,
                                                                        FetchDirection direction) {
        List<MessageResponse> messages = channelHistoryResponse.getMessages();

        Long nextCursor = null;
        boolean hasNext = messages.size() > limit;

        if (hasNext) {
            messages = messages.subList(0, limit);
            nextCursor = direction == FetchDirection.BEFORE ?
                    messages.getLast().getId() :
                    messages.getFirst().getId();
        }

//        if (hasNext) {
//            messages = messages.subList(0, limit);
//            nextCursor = direction == FetchDirection.BEFORE ?
//                    messages.getLast().getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() :
//                    messages.getFirst().getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
//        }

        return CursorResponse.<ChannelHistoryResponse>builder()
                             .items(channelHistoryResponse)
                             .nextCursor(nextCursor)
                             .hasMore(hasNext)
                             .build();
    }

//    private CursorResponse<ChannelHistoryResponse> createCursorResponse(ChannelHistoryResponse channelHistoryResponse,
//                                                                        int limit,
//                                                                        FetchDirection direction) {
//        Long nextCursor = null;
//        boolean hasNext = messages.size() > limit;
//
//        if (hasNext) {
//            messages = messages.subList(0, limit);
//            nextCursor = direction == FetchDirection.BEFORE ?
//                    messages.getLast().getId() :
//                    messages.getFirst().getId();
//        }
//
//        return CursorResponse.<ChannelHistoryResponse>builder()
//                             .items(ChannelHistoryResponse.builder().messages(messages).build())
//                             .nextCursor(nextCursor)
//                             .hasMore(hasNext)
//                             .build();
//    }

//    private CursorResponse<ChannelHistoryResponse> createCursorResponse(ChannelHistoryResponse channelHistoryResponse) {
//        Long nextCursor = channelHistoryResponse.getNextId();
//        boolean hasMore = nextCursor != null;
//
//        return CursorResponse.<ChannelHistoryResponse>builder()
//                             .items(channelHistoryResponse)
//                             .nextCursor(nextCursor)
//                             .hasMore(hasMore)
//                             .build();
//    }
}
