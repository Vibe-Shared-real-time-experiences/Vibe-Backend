package vn.vibeteam.vibe.dto.response.chat;

import lombok.*;

import java.io.Serializable;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class CategoryResponse implements Serializable {
    private long id;
    private long serverId;
    private String name;
    private int position;
    private boolean publicAccess;
    private boolean active;
    private Set<ChannelResponse> channels;
}
