package vn.vibeteam.vibe.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import vn.vibeteam.vibe.repository.chat.ChannelRepository;
import vn.vibeteam.vibe.repository.chat.ServerRepository;
import vn.vibeteam.vibe.util.SecurityUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketSessionListener {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ServerRepository serverRepository;
    private final ChannelRepository channelRepository;
    private final SecurityUtils securityUtils;

    private static final String USER_SUBSCRIPTIONS_KEY_PREFIX = "user:subscriptions:";
    private static final long SUBSCRIPTIONS_TTL_SECONDS = 60L; //

    /**
     * On WebSocket connect: Query user's servers and DM channels on store on redis
     */
    @EventListener
    public void handleConnectEvent(SessionConnectedEvent event) {
        try {
            long userId = securityUtils.getCurrentUserId();
            log.info("WebSocket connected for userId: {}", userId);

            String subscriptionsKey = USER_SUBSCRIPTIONS_KEY_PREFIX + userId;

            List<Long> serverIds = serverRepository.findServerIdsByUserId(userId);
            List<Long> dmChannelIds = channelRepository.findDmChannelIdsByUserId(userId);

            redisTemplate.delete(subscriptionsKey);

            if (!serverIds.isEmpty()) {
                serverIds.forEach(serverId ->
                        redisTemplate.opsForSet().add(subscriptionsKey, "server:" + serverId)
                );
            }

            if (!dmChannelIds.isEmpty()) {
                dmChannelIds.forEach(channelId ->
                        redisTemplate.opsForSet().add(subscriptionsKey, "dm_channel:" + channelId)
                );
            }

            redisTemplate.expire(subscriptionsKey, SUBSCRIPTIONS_TTL_SECONDS, TimeUnit.SECONDS);

            log.info("User {} subscriptions stored in Redis: {} servers + {} DM channels",
                    userId, serverIds.size(), dmChannelIds.size());
        } catch (Exception e) {
            log.error("Error handling WebSocket connection", e);
        }
    }

    @EventListener
    public void handleDisconnectEvent(SessionDisconnectEvent event) {
        try {
            long userId = securityUtils.getCurrentUserId();
            log.info("WebSocket disconnected for userId: {}", userId);

            String subscriptionsKey = USER_SUBSCRIPTIONS_KEY_PREFIX + userId;
            redisTemplate.delete(subscriptionsKey);

            log.info("Cleaned up Redis subscriptions for userId: {}", userId);
        } catch (Exception e) {
            log.error("Error handling WebSocket disconnection", e);
        }
    }
}