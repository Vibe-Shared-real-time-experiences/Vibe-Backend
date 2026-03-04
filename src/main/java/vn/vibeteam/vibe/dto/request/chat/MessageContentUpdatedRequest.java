package vn.vibeteam.vibe.dto.request.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class MessageContentUpdatedRequest {
    private Long channelId;
    private Long messageId;
    private String newContent;
}
