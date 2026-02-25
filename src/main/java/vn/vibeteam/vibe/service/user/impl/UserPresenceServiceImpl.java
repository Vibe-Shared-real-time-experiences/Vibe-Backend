package vn.vibeteam.vibe.service.user.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import vn.vibeteam.vibe.common.UserPresenceStatus;
import vn.vibeteam.vibe.dto.event.UserPresenceEvent;
import vn.vibeteam.vibe.dto.response.user.UserPresenceResponse;
import vn.vibeteam.vibe.service.user.UserPresenceService;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPresenceServiceImpl implements UserPresenceService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ApplicationEventPublisher eventPublisher;

    private static final String PRESENCE_KEY_PREFIX = "user:presence:";
    private static final String USER_SUBSCRIPTIONS_KEY_PREFIX = "user:subscriptions:";
    private static final long PRESENCE_TTL_SECONDS = 30L;
    private static final long SUBSCRIPTIONS_TTL_SECONDS = 60L;

    @Override
    public void updateUserPresence(long userId, UserPresenceStatus status) {
        log.info("Updating user presence for userId: {} with status: {}", userId, status);

        String presenceKey = PRESENCE_KEY_PREFIX + userId;
        String subscriptionsKey = USER_SUBSCRIPTIONS_KEY_PREFIX + userId;

        try {
            if (status == UserPresenceStatus.ONLINE) {
                Boolean isNewlyOnline = redisTemplate.opsForValue()
                        .setIfAbsent(presenceKey, status.name(), PRESENCE_TTL_SECONDS, TimeUnit.SECONDS);

                if (Boolean.TRUE.equals(isNewlyOnline)) {
                    // Status changed from OFFLINE to ONLINE - broadcast
                    log.info("User {} came ONLINE - broadcasting event", userId);
                    broadcastPresenceUpdate(userId, status);
                } else {
                    // User was already ONLINE -> refresh TTLs (presence + subscriptions)
                    log.debug("User {} heartbeat received - refreshing TTLs", userId);
                    redisTemplate.expire(presenceKey, PRESENCE_TTL_SECONDS, TimeUnit.SECONDS);
                    redisTemplate.expire(subscriptionsKey, SUBSCRIPTIONS_TTL_SECONDS, TimeUnit.SECONDS);
                }
            } else {
                Boolean wasOnline = redisTemplate.delete(presenceKey);
                if (wasOnline) {
                    // Status changed from ONLINE to OFFLINE - broadcast
                    log.info("User {} went OFFLINE - broadcasting event", userId);
                    broadcastPresenceUpdate(userId, status);
                }
            }

            log.info("User presence updated successfully for userId: {}", userId);
        } catch (Exception e) {
            log.error("Error updating user presence for userId: {}", userId, e);
            throw new RuntimeException("Failed to update user presence", e);
        }
    }

    @Override
    public UserPresenceResponse getUserPresence(long userId) {
        log.info("Getting user presence for userId: {}", userId);

        String presenceKey = PRESENCE_KEY_PREFIX + userId;

        try {
            Object presenceObj = redisTemplate.opsForValue().get(presenceKey);

            UserPresenceStatus status = presenceObj == null ?
                    UserPresenceStatus.OFFLINE :
                    UserPresenceStatus.valueOf(presenceObj.toString());

            log.info("User presence retrieved for userId: {} with status: {}", userId, status);

            return UserPresenceResponse.builder()
                    .userId(userId)
                    .status(status)
                    .lastSeenAt(System.currentTimeMillis())
                    .build();
        } catch (Exception e) {
            log.error("Error getting user presence for userId: {}", userId, e);
            return UserPresenceResponse.builder()
                    .userId(userId)
                    .status(UserPresenceStatus.OFFLINE)
                    .lastSeenAt(System.currentTimeMillis())
                    .build();
        }
    }

//    @Override
//    public Set<UserPresenceResponse> getAllOnlineUsers() {
//        log.info("Getting all online users");
//
//        try {
//            Set<Object> onlineUsersObj = redisTemplate.opsForSet().members(ONLINE_USERS_KEY);
//
//            if (onlineUsersObj == null || onlineUsersObj.isEmpty()) {
//                log.debug("No online users found");
//                return Set.of();
//            }
//
//            Set<UserPresenceResponse> onlineUsers = onlineUsersObj.stream()
//                    .map(obj -> {
//                        long userId = Long.parseLong(obj.toString());
//                        return UserPresenceResponse.builder()
//                                .userId(userId)
//                                .status(UserPresenceStatus.ONLINE)
//                                .lastSeenAt(System.currentTimeMillis())
//                                .build();
//                    })
//                    .collect(Collectors.toSet());
//
//            log.info("Retrieved {} online users", onlineUsers.size());
//            return onlineUsers;
//        } catch (Exception e) {
//            log.error("Error getting all online users", e);
//            return Set.of();
//        }
//    }

    private void broadcastPresenceUpdate(long userId, UserPresenceStatus status) {
        log.info("Broadcasting presence update for userId: {} with status: {}", userId, status);

        try {
            UserPresenceEvent event = UserPresenceEvent.builder()
                    .userId(userId)
                    .status(status)
                    .timestamp(System.currentTimeMillis())
                    .build();

            eventPublisher.publishEvent(event);
            log.info("Presence update event published for userId: {}", userId);
        } catch (Exception e) {
            log.error("Error broadcasting presence update for userId: {}", userId, e);
            throw new RuntimeException("Failed to broadcast presence update", e);
        }
    }
}
