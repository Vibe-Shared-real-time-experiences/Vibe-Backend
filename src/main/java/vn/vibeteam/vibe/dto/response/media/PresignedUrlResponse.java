package vn.vibeteam.vibe.dto.response.media;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class PresignedUrlResponse {
    private String url;
    private String objectKey;
}
