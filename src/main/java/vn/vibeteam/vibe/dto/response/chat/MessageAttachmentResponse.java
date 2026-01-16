package vn.vibeteam.vibe.dto.response.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.vibeteam.vibe.common.MessageAttachmentType;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class MessageAttachmentResponse {
    private String url;
    private MessageAttachmentType type; // "IMAGE", "VIDEO", "FILE"
    private String contentType; // "image/png", "video/mp4"
    private int width;
    private int height;
    private long size;
}
