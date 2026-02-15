package vn.vibeteam.vibe.repository.chat.cache;

import vn.vibeteam.vibe.dto.response.chat.ChannelResponse;

public interface ChannelCacheRepository {
    ChannelResponse getChannelById(Long channelId);
    void saveChannel(ChannelResponse channel);
}
