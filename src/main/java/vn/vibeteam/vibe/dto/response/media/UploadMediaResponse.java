package vn.vibeteam.vibe.dto.response.media;

import lombok.*;
import vn.vibeteam.vibe.common.MessageAttachmentType;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UploadMediaResponse {
    private String url;
    private MessageAttachmentType type;
    private String contentType; // "image/png", "video/mp4"
    private Integer width;
    private Integer height;
    private Long size;
}
