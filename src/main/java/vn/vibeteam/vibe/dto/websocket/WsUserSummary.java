package vn.vibeteam.vibe.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@Builder
@Getter
public class WsUserSummary {
    private Long id;
    private String username;
    private String avatarUrl;
}