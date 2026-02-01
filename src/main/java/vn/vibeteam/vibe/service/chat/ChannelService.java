package vn.vibeteam.vibe.service.chat;

import vn.vibeteam.vibe.dto.request.chat.CreateChannelRequest;
import vn.vibeteam.vibe.dto.response.chat.ChannelResponse;

import java.util.List;

public interface ChannelService {
    ChannelResponse createChannel(long userId, long serverId, CreateChannelRequest createChannelRequest);
    List<ChannelResponse> listChannelsByServerId(long serverId);
    ChannelResponse getChannelById(long serverId, long channelId);
    void deleteChannel(long userId, long channelId);
}
