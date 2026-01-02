package vn.vibeteam.vibe.dto.request.authentication;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class LoginRequest {
    private final String username;
    private final String password;
}
