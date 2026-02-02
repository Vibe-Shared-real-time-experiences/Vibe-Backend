package vn.vibeteam.vibe.service.chat;

import vn.vibeteam.vibe.dto.request.chat.CreateChannelRequest;
import vn.vibeteam.vibe.dto.response.chat.ChannelResponse;
import vn.vibeteam.vibe.dto.response.user.UserReadStateResponse;

import java.util.List;

public interface ChannelService {
    ChannelResponse createChannel(Long userId, Long serverId, CreateChannelRequest createChannelRequest);
    List<ChannelResponse> listChannelsByServerId(Long serverId);
    ChannelResponse getChannelById(Long serverId, Long channelId);
    UserReadStateResponse getUserReadStateInChannel(Long userId, Long channelId);
    void updateUserReadStateInChannel(Long userId, Long channelId, Long messageId);
    void deleteChannel(Long userId, Long channelId);

}
