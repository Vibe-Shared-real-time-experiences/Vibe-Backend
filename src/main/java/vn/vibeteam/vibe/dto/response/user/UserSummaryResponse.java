package vn.vibeteam.vibe.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class UserSummaryResponse {
    private Long id;
    private String username;
    private String avatarUrl;
}