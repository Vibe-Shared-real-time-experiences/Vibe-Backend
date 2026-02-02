package vn.vibeteam.vibe.service.auth.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.vibeteam.vibe.common.ChannelType;
import vn.vibeteam.vibe.exception.AppException;
import vn.vibeteam.vibe.exception.ErrorCode;
import vn.vibeteam.vibe.model.channel.Channel;
import vn.vibeteam.vibe.repository.chat.ChannelMemberRepository;
import vn.vibeteam.vibe.repository.chat.ChannelRepository;
import vn.vibeteam.vibe.repository.chat.ServerMemberRepository;
import vn.vibeteam.vibe.service.auth.ChannelGatekeeper;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChannelGatekeeperImpl implements ChannelGatekeeper {

    private final ChannelRepository channelRepo;
    private final ServerMemberRepository serverMemberRepo;
    private final ChannelMemberRepository channelMemberRepo;

    @Override
    public Channel validateChannelAccess(Long userId, Long channelId) {
        // 1. Fetch channel
        Channel channel = channelRepo.findById(channelId)
                                     .orElseThrow(() -> new AppException(ErrorCode.CHANNEL_NOT_FOUND));

        boolean hasAccess = false;

        // 2. Check access based on channel type
        if (channel.getType() == ChannelType.DM || channel.getType() == ChannelType.GROUP_DM) {
            hasAccess = channelMemberRepo.existsByChannelIdAndUserId(channelId, userId);
        } else {
            hasAccess = serverMemberRepo.existsByServerIdAndUserId(channel.getServer().getId(), userId);
        }

        if (!hasAccess) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACTION);
        }

        return channel;
    }
}
