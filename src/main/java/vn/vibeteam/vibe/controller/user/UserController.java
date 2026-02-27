package vn.vibeteam.vibe.controller.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import vn.vibeteam.vibe.dto.common.ApiResponse;
import vn.vibeteam.vibe.dto.request.user.CreateUserProfileRequest;
import vn.vibeteam.vibe.dto.request.user.UpdateUserProfileRequest;
import vn.vibeteam.vibe.dto.request.user.UserSummaryRequest;
import vn.vibeteam.vibe.dto.response.user.UserPresenceResponse;
import vn.vibeteam.vibe.dto.response.user.UserProfileResponse;
import vn.vibeteam.vibe.dto.response.user.UserSummaryResponse;
import vn.vibeteam.vibe.service.user.UserProfileService;
import vn.vibeteam.vibe.util.SecurityUtils;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserProfileService userProfileService;

    @PostMapping("")
    public ApiResponse<List<UserSummaryResponse>> getUserSummaryInfo(@RequestBody UserSummaryRequest request) {
        log.info("Get user summary info request for userIds: {}", request.getUserIds());

        List<UserSummaryResponse> userSummaryInfo = userProfileService.getUserSummaryInfo(request);

        return ApiResponse.<List<UserSummaryResponse>>builder()
                          .code(200)
                          .message("User presence retrieved successfully")
                          .data(userSummaryInfo)
                          .build();
    }
}
