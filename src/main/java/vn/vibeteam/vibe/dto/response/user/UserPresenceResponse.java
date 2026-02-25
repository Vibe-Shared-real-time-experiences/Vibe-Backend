package vn.vibeteam.vibe.dto.response.user;

import lombok.*;
import vn.vibeteam.vibe.common.UserPresenceStatus;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class UserPresenceResponse {
    private Long userId;
    private UserPresenceStatus status;
    private Long lastSeenAt;
}

