package vn.vibeteam.vibe.dto.response.user;

import lombok.*;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class UserReadStateResponse {
    private Long channelId;
    private Long userId;
    private Long lastMessageId;
    private Long lastReadMessageId;
    private Long unreadCount;
    private Boolean unread;
    private LocalDateTime lastReadAt;
}
