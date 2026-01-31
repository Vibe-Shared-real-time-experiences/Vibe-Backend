package vn.vibeteam.vibe.service.auth;

import vn.vibeteam.vibe.dto.request.authentication.LoginRequest;
import vn.vibeteam.vibe.dto.request.authentication.LogoutRequest;
import vn.vibeteam.vibe.dto.request.authentication.RefreshRequest;
import vn.vibeteam.vibe.dto.request.authentication.RegisterRequest;
import vn.vibeteam.vibe.dto.response.authentication.AuthenticationResponse;

public interface AuthenticationService {
    AuthenticationResponse login(LoginRequest loginRequest);
    AuthenticationResponse register(RegisterRequest registerRequest);
    AuthenticationResponse refreshToken(RefreshRequest refreshRequest);
    Boolean logout(LogoutRequest logoutRequest);
}
