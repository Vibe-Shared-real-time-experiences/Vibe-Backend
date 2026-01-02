package vn.vibeteam.vibe.dto.response.authentication;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AuthenticationResponse {
    private String accessToken;
    private String refreshToken;
}
