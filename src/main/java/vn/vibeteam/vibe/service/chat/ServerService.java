package vn.vibeteam.vibe.service.chat;

import vn.vibeteam.vibe.dto.request.chat.CreateServerRequest;
import vn.vibeteam.vibe.dto.response.chat.ChannelUnreadResponse;
import vn.vibeteam.vibe.dto.response.chat.ServerDetailResponse;
import vn.vibeteam.vibe.dto.response.chat.ServerResponse;

import java.util.List;

public interface ServerService {
    ServerDetailResponse createServer(Long userId, CreateServerRequest createServerRequest);
    ServerDetailResponse getServerById(Long userId, Long serverId);
    List<ServerResponse> getUserServers(Long userId);
    List<ChannelUnreadResponse> getUserReadStateInServer(Long userId, Long serverId);
    void joinServer(Long userId, Long serverId);
    void leaveServer(Long userId, Long serverId);
    void deleteServer(Long userId, Long serverId);
}
