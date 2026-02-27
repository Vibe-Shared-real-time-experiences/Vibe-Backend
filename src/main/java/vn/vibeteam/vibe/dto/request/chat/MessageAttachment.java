package vn.vibeteam.vibe.dto.request.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import vn.vibeteam.vibe.common.MessageAttachmentType;

@RequiredArgsConstructor
@Getter
@Setter
public class MessageAttachment {
    private String objectKey;
    private MessageAttachmentType type;
    private String contentType; // "image/png", "video/mp4"
    private Integer width;
    private Integer height;
    private Long size;
}
