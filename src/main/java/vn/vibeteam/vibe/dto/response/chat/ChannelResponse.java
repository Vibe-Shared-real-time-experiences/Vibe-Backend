package vn.vibeteam.vibe.dto.response.chat;

import lombok.*;
import vn.vibeteam.vibe.common.ChannelType;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class ChannelResponse implements Serializable {
    private long id;
    private long serverId;
    private Long categoryId;
    private String name;
    private ChannelType type;
    private int position;
}
