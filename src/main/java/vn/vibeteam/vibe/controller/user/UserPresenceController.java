package vn.vibeteam.vibe.controller.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import vn.vibeteam.vibe.dto.common.ApiResponse;
import vn.vibeteam.vibe.dto.response.user.UserPresenceResponse;
import vn.vibeteam.vibe.service.user.UserPresenceService;
import vn.vibeteam.vibe.util.SecurityUtils;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/presence")
@RequiredArgsConstructor
@Slf4j
public class UserPresenceController {

    private final UserPresenceService userPresenceService;
    private final SecurityUtils securityUtils;

    @GetMapping("")
    public ApiResponse<UserPresenceResponse> getUserPresence() {
        long userId = securityUtils.getCurrentUserId();
        log.info("Get user presence request for userId: {}", userId);

        UserPresenceResponse response = userPresenceService.getUserPresence(userId);

        return ApiResponse.<UserPresenceResponse>builder()
                .code(200)
                .message("User presence retrieved successfully")
                .data(response)
                .build();
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserPresenceResponse> getUserPresenceByUserId(
            @PathVariable long userId) {
        log.info("Get user presence request for userId: {}", userId);

        UserPresenceResponse response = userPresenceService.getUserPresence(userId);

        return ApiResponse.<UserPresenceResponse>builder()
                .code(200)
                .message("User presence retrieved successfully")
                .data(response)
                .build();
    }

//    @GetMapping("/online/list")
//    public ApiResponse<Set<UserPresenceResponse>> getAllOnlineUsers() {
//        log.info("Get all online users request");
//
//        Set<UserPresenceResponse> onlineUsers = userPresenceService.getAllOnlineUsers();
//
//        return ApiResponse.<Set<UserPresenceResponse>>builder()
//                .code(200)
//                .message("Online users list retrieved successfully")
//                .data(onlineUsers)
//                .build();
//    }
}
