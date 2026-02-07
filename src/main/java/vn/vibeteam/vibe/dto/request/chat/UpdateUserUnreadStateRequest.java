package vn.vibeteam.vibe.dto.request.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class UpdateUserUnreadStateRequest {
    private Long lastReadMessageId;
}
