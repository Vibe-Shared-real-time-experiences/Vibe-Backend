package vn.vibeteam.vibe.dto.event;

import lombok.*;
import vn.vibeteam.vibe.common.UserPresenceStatus;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class UserPresenceEvent implements Serializable {
    private Long userId;
    private UserPresenceStatus status;
    private Long timestamp;
}

