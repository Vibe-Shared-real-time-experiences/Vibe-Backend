package vn.vibeteam.vibe.repository.chat.cache.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import vn.vibeteam.vibe.dto.response.chat.ServerResponse;
import vn.vibeteam.vibe.repository.chat.cache.ServerCacheRepository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ServerCacheRepositoryImpl implements ServerCacheRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String KEY_PREFIX = "server:%d";
    private static final long CACHE_TTL_MILISECOND =  5 * 60 * 1000; // 5 minutes in milliseconds

    @Override
    public ServerResponse getServerById(Long serverId) {
        String key = String.format(KEY_PREFIX, serverId);

        Object cachedObj = redisTemplate.opsForValue().get(key);
        if (cachedObj == null) {
            return null;
        }

        return (ServerResponse) cachedObj;
    }

    @Override
    public void saveServer(ServerResponse serverResponse) {
        if (serverResponse == null) return;

        String key = String.format(KEY_PREFIX, serverResponse.getId());
        redisTemplate.opsForValue().set(key,
                                        serverResponse,
                                        CACHE_TTL_MILISECOND,
                                        TimeUnit.MILLISECONDS);
    }

    @Override
    public Long getServerMemberId(Long serverId, Long userId) {
        if (serverId == null || userId == null) return null;

        String key = String.format(KEY_PREFIX+":member:%d", serverId, userId);
        Object object = redisTemplate.opsForValue().get(key);
        if (object == null) return null;

        return Long.valueOf(object.toString());
    }

    @Override
    public void saveServerMemberId(Long serverId, Long serverMemberId) {
        if (serverId == null || serverMemberId == null) return;

        String key = String.format(KEY_PREFIX+":member:%d", serverId, serverMemberId);
        redisTemplate.opsForValue().set(key,
                                        serverMemberId,
                                        CACHE_TTL_MILISECOND,
                                        TimeUnit.MILLISECONDS);
    }
}
