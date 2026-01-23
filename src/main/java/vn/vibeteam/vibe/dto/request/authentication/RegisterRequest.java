package vn.vibeteam.vibe.dto.request.authentication;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@RequiredArgsConstructor
@Getter
public class RegisterRequest {
    private final String email;
    private final String password;
    private final String confirmPassword;
    private final String displayName;
    private final LocalDate dateOfBirth;
}
