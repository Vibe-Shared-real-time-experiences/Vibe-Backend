package vn.vibeteam.vibe.dto.request.chat;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class CreateServerRequest {
    private String name;
    private String description;
    private String iconUrl;
    private Boolean publicAccess;
}
