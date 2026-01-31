package vn.vibeteam.vibe.dto.request.media;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import vn.vibeteam.vibe.common.MediaType;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UploadMediaRequest {
    private MultipartFile file;
    private MediaType type;
    private String id;
}