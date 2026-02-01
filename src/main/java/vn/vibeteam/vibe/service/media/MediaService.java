package vn.vibeteam.vibe.service.media;

import vn.vibeteam.vibe.dto.request.media.UploadMediaRequest;
import vn.vibeteam.vibe.dto.response.media.UploadMediaResponse;

public interface MediaService {
    UploadMediaResponse uploadFile(long userId, UploadMediaRequest file);
}
