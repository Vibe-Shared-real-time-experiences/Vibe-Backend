package vn.vibeteam.vibe.controller.media;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.vibeteam.vibe.dto.common.ApiResponse;
import vn.vibeteam.vibe.dto.request.media.UploadMediaRequest;
import vn.vibeteam.vibe.dto.response.media.UploadMediaResponse;
import vn.vibeteam.vibe.service.media.MediaService;
import vn.vibeteam.vibe.util.SecurityUtils;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;
    private final SecurityUtils securityUtils;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ApiResponse<UploadMediaResponse> upload(@ModelAttribute UploadMediaRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        UploadMediaResponse uploadMediaResponse = mediaService.uploadFile(userId, request);

        return ApiResponse.<UploadMediaResponse>builder()
                .code(200)
                .data(uploadMediaResponse)
                .message("File uploaded successfully")
                .build();
    }
}