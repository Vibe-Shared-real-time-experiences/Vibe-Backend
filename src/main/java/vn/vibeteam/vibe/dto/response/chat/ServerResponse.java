package vn.vibeteam.vibe.dto.response.chat;

import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class ServerResponse implements Serializable {
    private long id;
    private long ownerId;
    private String name;
    private String description;
    private String iconUrl;
    private boolean publicAccess;
    private boolean active;
}
