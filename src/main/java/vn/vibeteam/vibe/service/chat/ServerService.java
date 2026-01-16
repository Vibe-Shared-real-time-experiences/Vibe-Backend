package vn.vibeteam.vibe.service.chat;

import vn.vibeteam.vibe.dto.request.chat.CreateServerRequest;
import vn.vibeteam.vibe.dto.response.chat.ServerDetailResponse;
import vn.vibeteam.vibe.dto.response.chat.ServerResponse;

import java.util.List;

public interface ServerService {
    ServerDetailResponse createServer(CreateServerRequest createServerRequest);
    ServerDetailResponse getServerById(long serverId);
    List<ServerResponse> getUserServers();
    void joinServer(long serverId);
    void leaveServer(long serverId);
    void deleteServer(long serverId);
}
