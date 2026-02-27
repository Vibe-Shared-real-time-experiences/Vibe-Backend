package vn.vibeteam.vibe.service.media;

import vn.vibeteam.vibe.dto.request.media.GenerateUrlRequest;
import vn.vibeteam.vibe.dto.request.media.UploadMediaRequest;
import vn.vibeteam.vibe.dto.response.media.PresignedUrlResponse;
import vn.vibeteam.vibe.dto.response.media.UploadMediaResponse;

public interface MediaService {
    PresignedUrlResponse getPresignedUrl(long userId, GenerateUrlRequest request);
    UploadMediaResponse uploadFile(long userId, UploadMediaRequest file);
}
