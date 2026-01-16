package vn.vibeteam.vibe.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import vn.vibeteam.vibe.common.MessageAttachmentType;

@AllArgsConstructor
@Builder
@Getter
public class WsAttachmentResponse {
    private final String url;
    private MessageAttachmentType type; // "IMAGE", "VIDEO", "FILE"
    private String contentType; // "image/png", "video/mp4"
    private Integer width;
    private Integer height;
    private Long size;
}
