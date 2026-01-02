package vn.vibeteam.vibe.dto.request.authentication;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class LogoutRequest {
    private final String accessToken;
    private final String refreshToken;
}
