package vn.vibeteam.vibe.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class UserProfileResponse {
    private Long id;
    private String displayName;
    private LocalDate dateOfBirth;
    private String avatarUrl;
    private String bio;
    private Boolean isPublic;
}
