package vn.vibeteam.vibe.controller.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.vibeteam.vibe.dto.common.ApiResponse;
import vn.vibeteam.vibe.dto.request.authentication.LoginRequest;
import vn.vibeteam.vibe.dto.request.authentication.LogoutRequest;
import vn.vibeteam.vibe.dto.request.authentication.RefreshRequest;
import vn.vibeteam.vibe.dto.request.authentication.RegisterRequest;
import vn.vibeteam.vibe.dto.response.authentication.AuthenticationResponse;
import vn.vibeteam.vibe.service.auth.AuthenticationService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    /* Test endpoint to verify authentication and authorization */
    @GetMapping("/hello")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> hello() {
        log.info("Hello endpoint called");

        return ApiResponse.<String>builder()
                .code(200)
                .message("Hello, World!")
                .data("Hello, World!")
                .build();
    }

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> login(@RequestBody LoginRequest loginRequest) {
        AuthenticationResponse response = authenticationService.login(loginRequest);

        return ApiResponse.<AuthenticationResponse>builder()
                .code(200)
                .message("Login successful")
                .data(response)
                .build();
    }

    @PostMapping("/register")
    public ApiResponse<AuthenticationResponse> register(@RequestBody RegisterRequest registerRequest) {
        AuthenticationResponse response = authenticationService.register(registerRequest);

        return ApiResponse.<AuthenticationResponse>builder()
                          .code(200)
                          .message("Login successful")
                          .data(response)
                          .build();
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> refreshToken(@RequestBody RefreshRequest refreshRequest) {
        AuthenticationResponse response = authenticationService.refreshToken(refreshRequest);

        return ApiResponse.<AuthenticationResponse>builder()
                          .code(200)
                          .message("Token refreshed successfully")
                          .data(response)
                          .build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody LogoutRequest logoutRequest) {
        Boolean response = authenticationService.logout(logoutRequest);

        return ApiResponse.<Void>builder()
                          .code(200)
                          .message("Logout successful")
                          .build();
    }
}
