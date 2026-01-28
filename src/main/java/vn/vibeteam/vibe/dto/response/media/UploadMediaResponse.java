package vn.vibeteam.vibe.dto.response.media;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import vn.vibeteam.vibe.common.MessageAttachmentType;

@RequiredArgsConstructor
@Getter
@Setter
public class UploadMediaResponse {
    private String url;
    private MessageAttachmentType type;
    private String contentType; // "image/png", "video/mp4"
    private Integer width;
    private Integer height;
    private Long size;
}
