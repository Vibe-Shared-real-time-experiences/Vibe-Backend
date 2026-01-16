package vn.vibeteam.vibe.dto.request.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import vn.vibeteam.vibe.common.ChannelType;

@RequiredArgsConstructor
@Getter
@Setter
public class CreateChannelRequest {
    private final String categoryId;
    private final String name;
    private final ChannelType type;
    private final Boolean publicAccess;
}
