package vn.vibeteam.vibe.controller.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import vn.vibeteam.vibe.common.UserPresenceStatus;
import vn.vibeteam.vibe.service.user.UserPresenceService;
import vn.vibeteam.vibe.util.SecurityUtils;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UserPresenceWebSocketHandler {

    private final UserPresenceService userPresenceService;
    private final SecurityUtils securityUtils;

    @MessageMapping("/presence/heartbeat")
    public void handleHeartbeat() {
        long userId = securityUtils.getCurrentUserId();
        log.info("Heartbeat received via WebSocket for userId: {}", userId);

        try {
            UserPresenceStatus status = UserPresenceStatus.ONLINE;
            userPresenceService.updateUserPresence(userId, status);
        } catch (Exception e) {
            log.error("Error processing WebSocket heartbeat for userId: {}", userId, e);
            throw e;
        }
    }
}

