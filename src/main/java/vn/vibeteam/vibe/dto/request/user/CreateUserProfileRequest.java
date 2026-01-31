package vn.vibeteam.vibe.dto.request.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@RequiredArgsConstructor
@Getter
public class CreateUserProfileRequest {
    private final String displayName;
    private final LocalDate dateOfBirth;
    private final String avatarUrl;
    private final String bio;
    private final Boolean isPublic;
}

