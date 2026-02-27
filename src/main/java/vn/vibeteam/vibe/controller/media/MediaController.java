package vn.vibeteam.vibe.controller.media;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import vn.vibeteam.vibe.dto.common.ApiResponse;
import vn.vibeteam.vibe.dto.request.media.GenerateUrlRequest;
import vn.vibeteam.vibe.dto.request.media.UploadMediaRequest;
import vn.vibeteam.vibe.dto.response.media.PresignedUrlResponse;
import vn.vibeteam.vibe.dto.response.media.UploadMediaResponse;
import vn.vibeteam.vibe.service.media.MediaService;
import vn.vibeteam.vibe.util.SecurityUtils;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
@Slf4j
public class MediaController {

    private final MediaService mediaService;
    private final SecurityUtils securityUtils;

    @PostMapping(value = "/presigned-url")
    public ApiResponse<PresignedUrlResponse> upload(@RequestBody GenerateUrlRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        log.info("Generate presigned URL request for userId: {}, mediaType: {}",
                userId, request.getType());

        PresignedUrlResponse presignedUrl = mediaService.getPresignedUrl(userId, request);

        return ApiResponse.<PresignedUrlResponse>builder()
                          .code(200)
                          .data(presignedUrl)
                          .message("Presigned URL generated successfully")
                          .build();
    }

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