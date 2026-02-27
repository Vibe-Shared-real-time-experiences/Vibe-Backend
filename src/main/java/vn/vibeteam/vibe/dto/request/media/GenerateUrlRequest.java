package vn.vibeteam.vibe.dto.request.media;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.vibeteam.vibe.common.MediaType;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GenerateUrlRequest {
    private String fileName;
    private String contentType;
    private MediaType type;
}
