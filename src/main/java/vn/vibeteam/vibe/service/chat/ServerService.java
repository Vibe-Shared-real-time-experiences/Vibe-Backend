package vn.vibeteam.vibe.service.chat;

import vn.vibeteam.vibe.dto.request.chat.CreateServerRequest;
import vn.vibeteam.vibe.dto.response.chat.ServerDetailResponse;
import vn.vibeteam.vibe.dto.response.chat.ServerResponse;

import java.util.List;

public interface ServerService {
    ServerDetailResponse createServer(long userId, CreateServerRequest createServerRequest);
    ServerDetailResponse getServerById(long userId, long serverId);
    List<ServerResponse> getUserServers(long userId);
    void joinServer(long userId, long serverId);
    void leaveServer(long userId, long serverId);
    void deleteServer(long userId, long serverId);
}
