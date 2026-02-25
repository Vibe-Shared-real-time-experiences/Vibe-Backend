package vn.vibeteam.vibe.service.user;

import vn.vibeteam.vibe.common.UserPresenceStatus;
import vn.vibeteam.vibe.dto.response.user.UserPresenceResponse;

import java.util.Set;

public interface UserPresenceService {
    void updateUserPresence(long userId, UserPresenceStatus status);
    UserPresenceResponse getUserPresence(long userId);
//    Set<UserPresenceResponse> getAllOnlineUsers();
}
