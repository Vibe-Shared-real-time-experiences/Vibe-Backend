package vn.vibeteam.vibe.service.auth;

import vn.vibeteam.vibe.model.channel.Channel;

public interface ChannelGatekeeper {
    Channel validateChannelAccess(Long userId, Long channelId);
}
