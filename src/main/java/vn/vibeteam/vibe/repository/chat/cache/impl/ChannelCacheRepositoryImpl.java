package vn.vibeteam.vibe.repository.chat.cache.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;
import vn.vibeteam.vibe.dto.response.chat.ChannelResponse;
import vn.vibeteam.vibe.repository.chat.cache.ChannelCacheRepository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ChannelCacheRepositoryImpl implements ChannelCacheRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String KEY_PREFIX = "channel:%d";
    private static final long CACHE_TTL_MILISECOND =  5 * 60 * 1000; // 5 minutes in milliseconds

    @Override
    public ChannelResponse getChannelById(Long channelId) {
        String key = String.format(KEY_PREFIX, channelId);

        Object object = redisTemplate.opsForValue().get(key);
        if (object == null) {
            return null;
        }

        return (ChannelResponse) object;
    }

    @Override
    public void saveChannel(ChannelResponse channelResponse) {
        if (channelResponse == null) return;

        String key = String.format(KEY_PREFIX, channelResponse.getId());
        redisTemplate.opsForValue().set(key,
                                        channelResponse,
                                        CACHE_TTL_MILISECOND,
                                        TimeUnit.MILLISECONDS);

    }
}
