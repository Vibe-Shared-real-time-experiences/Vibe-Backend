package vn.vibeteam.vibe.dto.response.chat;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class CategoryResponse implements Serializable {
    private Long id;
    private Long serverId;
    private String name;
    private Integer position;
    private Boolean publicAccess;
    private Boolean active;
    private List<ChannelResponse> channels;
}
