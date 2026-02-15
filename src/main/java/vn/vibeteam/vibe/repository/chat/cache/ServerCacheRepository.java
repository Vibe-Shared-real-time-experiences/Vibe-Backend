package vn.vibeteam.vibe.repository.chat.cache;

import vn.vibeteam.vibe.dto.response.chat.ServerResponse;

public interface ServerCacheRepository {
    ServerResponse getServerById(Long serverId);
    void saveServer(ServerResponse serverResponse);
    Long getServerMemberId(Long serverId, Long userId);
    void saveServerMemberId(Long serverId, Long serverMemberId);
}
