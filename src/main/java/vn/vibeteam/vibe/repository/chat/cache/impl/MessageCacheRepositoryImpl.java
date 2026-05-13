package vn.vibeteam.vibe.repository.chat.cache.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;
import vn.vibeteam.vibe.common.FetchDirection;
import vn.vibeteam.vibe.dto.response.chat.MessageResponse;
import vn.vibeteam.vibe.repository.chat.cache.MessageCacheRepository;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class MessageCacheRepositoryImpl implements MessageCacheRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String KEY_PREFIX = "channel:%d:messages";
    private static final long CACHE_TTL_MILISECOND = 5 * 60 * 1000; // 5 minutes in milliseconds

    @Override
    public List<MessageResponse> getMessages(Long channelId, Long cursor, FetchDirection direction, int limit) {
        String key = String.format(KEY_PREFIX, channelId);
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();

        long maxScore = 0;

        if (cursor == null) {
            maxScore = Long.MAX_VALUE;
        } else {
            maxScore = cursor >> 22;
            if (direction == FetchDirection.BEFORE) {
                maxScore -= 1;
            } else
                if (direction == FetchDirection.AFTER) {
                    maxScore += 1;
                }
        }

        // Fetch logic: Fetch from maxScore down to minScore with limit
        Set<Object> rawObjects = zSetOps.reverseRangeByScore(key, Double.NEGATIVE_INFINITY, maxScore, 0, limit);

        if (rawObjects == null || rawObjects.isEmpty()) {
            return Collections.emptyList();
        }

        return rawObjects.stream()
                         .map(obj -> (MessageResponse) obj)
                         .collect(Collectors.toList());
    }

    @Override
    public void saveMessages(Long channelId, List<MessageResponse> messages) {
        if (messages == null || messages.isEmpty()) return;

        String key = String.format(KEY_PREFIX, channelId);
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();

        for (MessageResponse msg : messages) {
            Long score = msg.getId() >> 22;
            zSetOps.addIfAbsent(key, msg, score);
        }

        // Reset TTL
        redisTemplate.expire(key, CACHE_TTL_MILISECOND, TimeUnit.MILLISECONDS);
    }

    @Override
    public void saveMessage(Long channelId, MessageResponse message) {
        String key = String.format(KEY_PREFIX, channelId);

        Long score = message.getId() >> 22;
        redisTemplate.opsForZSet().add(key, message, score);

        // Reset TTL
        redisTemplate.expire(key, CACHE_TTL_MILISECOND, TimeUnit.MILLISECONDS);
    }

    @Override
    public void updateMessageContent(Long channelId, Long messageId, String newContent) {
        String key = String.format(KEY_PREFIX, channelId);
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();

        Long score = messageId >> 22;
        Set<Object> rawObjects = zSetOps.rangeByScore(key, score, score);

        if (rawObjects == null || rawObjects.isEmpty()) {
            log.warn("Message with id {} not found in cache for channel {}", messageId, channelId);
            return;
        }

        Object rawObj = rawObjects.iterator().next();
        zSetOps.removeRangeByScore(key, score, score);

        MessageResponse msg = (MessageResponse) rawObj;
        msg.setContent(newContent);
        zSetOps.add(key, msg, score);

        redisTemplate.expire(key, CACHE_TTL_MILISECOND, TimeUnit.MILLISECONDS);
    }

    @Override
    public void deleteMessage(Long channelId, Long messageId) {
        String key = String.format(KEY_PREFIX, channelId);
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();

        Long score = messageId >> 22;
        Set<Object> rawObjects = zSetOps.rangeByScore(key, score, score);

        if (rawObjects == null || rawObjects.isEmpty()) {
            log.warn("Message with id {} not found in cache for channel {}", messageId, channelId);
            return;
        }

        zSetOps.removeRangeByScore(key, score, score);
        redisTemplate.expire(key, CACHE_TTL_MILISECOND, TimeUnit.MILLISECONDS);
    }
}