package vn.vibeteam.vibe.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@AllArgsConstructor
@Builder
@Getter
public class WsMessageResponse {
    private Long id;
    private String content;
    private Long channelId;
    private WsUserSummary author;
    private List<WsAttachmentResponse> attachments;
    private String createdAt;
}

