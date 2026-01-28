package vn.vibeteam.vibe.controller.media;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.vibeteam.vibe.dto.common.ApiResponse;
import vn.vibeteam.vibe.dto.request.media.UploadMediaRequest;
import vn.vibeteam.vibe.service.media.MediaService;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ApiResponse<String> upload(@ModelAttribute UploadMediaRequest request) {
        String url = mediaService.uploadFile(request);

        return ApiResponse.<String>builder()
                .code(200)
                .data(url)
                .message("File uploaded successfully")
                .build();
    }
}