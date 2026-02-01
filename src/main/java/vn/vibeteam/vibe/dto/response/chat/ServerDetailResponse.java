package vn.vibeteam.vibe.dto.response.chat;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class ServerDetailResponse implements Serializable {
    private Long id;
    private Long ownerId;
    private String name;
    private String description;
    private String iconUrl;
    private Boolean publicAccess;
    private Boolean active;
    private List<CategoryResponse> categories;
}
