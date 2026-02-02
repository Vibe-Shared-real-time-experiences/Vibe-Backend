package vn.vibeteam.vibe.dto.response.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class ChannelUnreadResponse {
    private Long channelId;
    private Long lastMessageId;
    private Long lastReadMessageId;
    private Boolean unread;
}