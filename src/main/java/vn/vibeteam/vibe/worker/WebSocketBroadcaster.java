package vn.vibeteam.vibe.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import vn.vibeteam.vibe.common.EventType;
import vn.vibeteam.vibe.dto.websocket.MessageBroadcastEvent;
import vn.vibeteam.vibe.dto.websocket.WsAttachmentResponse;
import vn.vibeteam.vibe.dto.websocket.WsEvent;
import vn.vibeteam.vibe.dto.websocket.WsMessageResponse;
import vn.vibeteam.vibe.model.channel.ChannelMessage;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;
    private static final String WEBSOCKET_SERVER_DESTINATION_PREFIX = "/topic/servers/";
    private static final String WEBSOCKET_CHANNEL_DESTINATION_PREFIX = "/topic/channels/";

    @EventListener
    public void onMessageCreated(MessageBroadcastEvent event) {
        log.info("Broadcasting message created event for channel: {}",
                 event.getChannelMessage().getChannel().getId());

        ChannelMessage channelMessage = event.getChannelMessage();
        Long serverId = event.getServerId();

        try {
            WsEvent<WsMessageResponse> wsChannelEvent = createWsChannelEvent(channelMessage);

            // Broadcast to channel subscribers
            messagingTemplate.convertAndSend(
                    WEBSOCKET_CHANNEL_DESTINATION_PREFIX + channelMessage.getChannel().getId(),
                    wsChannelEvent
            );

            // Broadcast to server subscribers
            WsEvent<String> wsServerEvent = WsEvent.<String>builder()
                                                   .eventType(EventType.MESSAGE_CREATED)
                                                   .data("New message in channel: " + channelMessage.getChannel().getName())
                                                   .build();
            messagingTemplate.convertAndSend(
                    WEBSOCKET_SERVER_DESTINATION_PREFIX + serverId,
                    wsServerEvent
            );

            log.info("Message event broadcasted successfully for messageId: {}", channelMessage.getId());
        } catch (Exception e) {
            log.error("Error broadcasting message event for messageId: {}", channelMessage.getId(), e);
        }
    }

    private WsEvent<WsMessageResponse> createWsChannelEvent(ChannelMessage channelMessage) {
        WsMessageResponse.WsMessageResponseBuilder wsMessageResponseBuilder =
                WsMessageResponse.builder()
                                 .id(channelMessage.getId())
                                 .content(channelMessage.getContent())
                                 .channelId(channelMessage.getChannel().getId())
                                 .createdAt(channelMessage.getCreatedAt().toString());

        if (channelMessage.getAttachmentMetadata() != null && !channelMessage.getAttachmentMetadata().isEmpty()) {
            wsMessageResponseBuilder.attachments(
                    channelMessage.getAttachmentMetadata().stream()
                                  .map(attachment -> WsAttachmentResponse.builder()
                                                                         .url(attachment.getUrl())
                                                                         .type(attachment.getType())
                                                                         .contentType(attachment.getContentType())
                                                                         .width(attachment.getWidth())
                                                                         .height(attachment.getHeight())
                                                                         .size(attachment.getSize())
                                                                         .build())
                                  .toList()
            );
        }

        return WsEvent.<WsMessageResponse>builder()
                      .eventType(EventType.MESSAGE_CREATED)
                      .data(wsMessageResponseBuilder.build())
                      .build();
    }
}

