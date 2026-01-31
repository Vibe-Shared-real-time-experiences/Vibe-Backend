package vn.vibeteam.vibe.dto.response.chat;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class ServerDetailResponse implements Serializable {
    private long id;
    private long ownerId;
    private String name;
    private String description;
    private String iconUrl;
    private boolean publicAccess;
    private boolean active;
    private List<CategoryResponse> categories;
}
