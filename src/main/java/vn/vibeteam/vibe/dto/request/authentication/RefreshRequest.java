package vn.vibeteam.vibe.dto.request.authentication;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class RefreshRequest {
    private String refreshToken;
}
