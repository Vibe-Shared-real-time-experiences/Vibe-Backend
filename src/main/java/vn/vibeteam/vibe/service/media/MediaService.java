package vn.vibeteam.vibe.service.media;

import vn.vibeteam.vibe.dto.request.media.UploadMediaRequest;

public interface MediaService {
    String uploadFile(UploadMediaRequest file);
}
