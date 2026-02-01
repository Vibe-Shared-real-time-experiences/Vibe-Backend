package vn.vibeteam.vibe.controller.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.vibeteam.vibe.dto.common.ApiResponse;
import vn.vibeteam.vibe.dto.request.user.CreateUserProfileRequest;
import vn.vibeteam.vibe.dto.request.user.UpdateUserProfileRequest;
import vn.vibeteam.vibe.dto.response.user.UserProfileResponse;
import vn.vibeteam.vibe.service.user.UserProfileService;
import vn.vibeteam.vibe.util.SecurityUtils;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@Slf4j
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final SecurityUtils securityUtils;

    @GetMapping("")
    public ApiResponse<UserProfileResponse> getUserProfile() {
        Long userId = securityUtils.getCurrentUserId();
        log.info("Get user profile request for userId: {}", userId);

        UserProfileResponse response = userProfileService.getUserProfile(userId);

        return ApiResponse.<UserProfileResponse>builder()
                .code(200)
                .message("User profile retrieved successfully")
                .data(response)
                .build();
    }

    @PostMapping("")
    public ApiResponse<UserProfileResponse> createUserProfile(
            @RequestBody CreateUserProfileRequest request) {

        Long userId = securityUtils.getCurrentUserId();
        log.info("Create user profile request for userId: {}", userId);

        UserProfileResponse response = userProfileService.createUserProfile(userId, request);

        return ApiResponse.<UserProfileResponse>builder()
                .code(201)
                .message("User profile created successfully")
                .data(response)
                .build();
    }

    @PutMapping("")
    public ApiResponse<UserProfileResponse> updateUserProfile(
            @RequestBody UpdateUserProfileRequest request) {

        Long userId = securityUtils.getCurrentUserId();
        log.info("Update user profile request for userId: {}", userId);

        UserProfileResponse response = userProfileService.updateUserProfile(userId, request);

        return ApiResponse.<UserProfileResponse>builder()
                .code(200)
                .message("User profile updated successfully")
                .data(response)
                .build();
    }
}
