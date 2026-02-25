package vn.vibeteam.vibe.worker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import vn.vibeteam.vibe.common.EventType;
import vn.vibeteam.vibe.common.UserPresenceStatus;
import vn.vibeteam.vibe.dto.event.UserPresenceEvent;
import vn.vibeteam.vibe.dto.response.user.UserPresenceResponse;
import vn.vibeteam.vibe.dto.websocket.WsEvent;
import vn.vibeteam.vibe.service.user.UserPresenceService;

import java.util.Set;

@Component
@Slf4j
public class WsUserPresenceEventListener extends KeyExpirationEventMessageListener {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private UserPresenceService userPresenceService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String PRESENCE_KEY_PREFIX = "user:presence:";
    private static final String USER_SUBSCRIPTIONS_KEY_PREFIX = "user:subscriptions:";

    private static final String SERVER_SUBSCRIPTION_PREFIX = "server:";
    private static final String DM_CHANNEL_SUBSCRIPTION_PREFIX = "dm_channel:";

    public WsUserPresenceEventListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    /**
     * Listen for Redis key expiration events to detect which user went offline
     * When user:presence:{userId} expires -> User went offline
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();

        if (expiredKey.startsWith(PRESENCE_KEY_PREFIX)) {
            String userId = expiredKey.split(":")[2];
            log.info("Detected user presence key expiration for userId: {}", userId);

            try {
                UserPresenceStatus offlineStatus = UserPresenceStatus.OFFLINE;
                userPresenceService.updateUserPresence(Long.parseLong(userId), offlineStatus);
            } catch (NumberFormatException e) {
                log.error("Invalid userId format in expired key: {}", userId, e);
            }
        }
    }

    @EventListener
    public void onUserPresenceEvent(UserPresenceEvent event) {
        long userId = event.getUserId();
        UserPresenceStatus status = event.getStatus();

        log.info("User presence event received: userId={}, status={}", userId, status);

        try {
            UserPresenceResponse response = userPresenceService.getUserPresence(userId);
            WsEvent<UserPresenceResponse> wsEvent = WsEvent.<UserPresenceResponse>builder()
                    .eventType(status == UserPresenceStatus.ONLINE ?
                            EventType.USER_ONLINE :
                            EventType.USER_OFFLINE)
                    .data(response)
                    .build();

            // Get user's subscriptions from Redis
            String subscriptionsKey = USER_SUBSCRIPTIONS_KEY_PREFIX + userId;
            Set<Object> subscriptions = redisTemplate.opsForSet().members(subscriptionsKey);

            if (subscriptions != null && !subscriptions.isEmpty()) {
                log.debug("User {} has {} subscriptions, broadcasting to each", userId, subscriptions.size());

                // Broadcast to each subscribed server/channel
                subscriptions.forEach(subscription -> {
                    String sub = subscription.toString();
                    String destination = null;

                    if (sub.startsWith(SERVER_SUBSCRIPTION_PREFIX)) {
                        long serverId = Long.parseLong(sub.substring(SERVER_SUBSCRIPTION_PREFIX.length()));
                        destination = "/topic/servers/" + serverId;
                    } else if (sub.startsWith(DM_CHANNEL_SUBSCRIPTION_PREFIX)) {
                        long channelId = Long.parseLong(sub.substring(DM_CHANNEL_SUBSCRIPTION_PREFIX.length()));
                        destination = "/topic/channels/" + channelId;
                    }

                    messagingTemplate.convertAndSend(destination, wsEvent);
                });

                log.info("User presence broadcasted to {} subscriptions (userId={}, status={})",
                        subscriptions.size(), userId, status);
            } else {
                log.info("No subscriptions found for user {} in Redis", userId);
            }
        } catch (Exception e) {
            log.error("Error broadcasting user presence for userId: {}", userId, e);
        }
    }
}
