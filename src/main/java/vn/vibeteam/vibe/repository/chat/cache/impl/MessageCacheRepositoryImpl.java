package vn.vibeteam.vibe.repository.chat.cache.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;
import vn.vibeteam.vibe.dto.response.chat.MessageResponse;
import vn.vibeteam.vibe.repository.chat.cache.MessageCacheRepository;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class MessageCacheRepositoryImpl implements MessageCacheRepository {

//    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "channel:%d:messages";
    private static final long CACHE_TTL_MILISECOND =  5 * 60 * 1000; // 5 minutes in milliseconds
    private final ObjectMapper objectMapper;

//    @Override
//    public List<MessageResponse> getMessages(Long channelId, Long cursor, int limit) {
//        limit += 1; // Get 1 extra to check if there's more data
//
//        String key = String.format(KEY_PREFIX, channelId);
//        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
//
//        // Determine maxScore based on cursor
//        double maxScore = (cursor == null)
//                ? Double.POSITIVE_INFINITY
//                : (cursor - 1);
//
//        // Fetch logic: Fetch from maxScore down to minScore with limit
//        Set<Object> rawObjects = zSetOps.reverseRangeByScore(key, Double.NEGATIVE_INFINITY, maxScore, 0, limit);
//
//        if (rawObjects == null || rawObjects.isEmpty()) {
//            return Collections.emptyList();
//        }
//
//        return rawObjects.stream()
//                         .map(obj -> (MessageResponse) obj)
//                         .collect(Collectors.toList());
//    }

    @Override
    public Set<String> getMessages(Long channelId, Long cursor, int limit) {
        limit += 1; // Get 1 extra to check if there's more data

        String key = String.format(KEY_PREFIX, channelId);
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

        // Determine maxScore based on cursor
        double maxScore = (cursor == null)
                ? Double.POSITIVE_INFINITY
                : (cursor - 1);

        // Fetch logic: Fetch from maxScore down to minScore with limit
        Set<String> rawObjects = zSetOps.reverseRangeByScore(key, Double.NEGATIVE_INFINITY, maxScore, 0, limit);

        if (rawObjects == null || rawObjects.isEmpty()) {
            return null;
        }

        return rawObjects;
    }

    @Override
    public void saveMessages(Long channelId, List<MessageResponse> messages) {
        if (messages == null || messages.isEmpty()) return;

        String key = String.format(KEY_PREFIX, channelId);
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

        for (MessageResponse msg : messages) {
            String json = objectMapper.writeValueAsString(msg);
            Long score = msg.getId();
            zSetOps.add(key, json, score);

//            zSetOps.add(key, msg, msg.getId());
        }

        // Reset TTL
        redisTemplate.expire(key, CACHE_TTL_MILISECOND, TimeUnit.MILLISECONDS);
    }

    @Override
    public void saveMessage(Long channelId, MessageResponse message) {
        String key = String.format(KEY_PREFIX, channelId);

        // Add new message to ZSet
//        redisTemplate.opsForZSet().add(key, message, message.getId());

        String json = objectMapper.writeValueAsString(message);
        Long score = message.getId();
        redisTemplate.opsForZSet().add(key, json, score);

        // Reset TTL
        redisTemplate.expire(key, CACHE_TTL_MILISECOND, TimeUnit.MILLISECONDS);
    }
}

//package vn.vibeteam.vibe.repository.chat.cache.impl;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.ZSetOperations;
//import org.springframework.stereotype.Repository;
//import tools.jackson.databind.ObjectMapper;
//import vn.vibeteam.vibe.dto.response.chat.MessageResponse;
//import vn.vibeteam.vibe.repository.chat.cache.MessageCacheRepository;
//
//import java.util.Collections;
//import java.util.List;
//import java.util.Set;
//import java.util.concurrent.TimeUnit;
//import java.util.stream.Collectors;
//
//@Repository
//@RequiredArgsConstructor
//@Slf4j
//public class MessageCacheRepositoryImpl implements MessageCacheRepository {
//
//    private final RedisTemplate<String, Object> redisTemplate;
//
//    private static final String KEY_PREFIX = "channel:%d:messages";
//    private static final long CACHE_TTL_MILISECOND =  5 * 60 * 1000; // 5 minutes in milliseconds
//
//    @Override
//    public List<MessageResponse> getMessages(Long channelId, Long cursor, int limit) {
//        limit += 1; // Get 1 extra to check if there's more data
//
//        String key = String.format(KEY_PREFIX, channelId);
//        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
//
//        // Determine maxScore based on cursor
//        double maxScore = (cursor == null)
//                ? Double.POSITIVE_INFINITY
//                : (cursor - 1);
//
//        // Fetch logic: Fetch from maxScore down to minScore with limit
//        Set<Object> rawObjects = zSetOps.reverseRangeByScore(key, Double.NEGATIVE_INFINITY, maxScore, 0, limit);
//
//        if (rawObjects == null || rawObjects.isEmpty()) {
//            return Collections.emptyList();
//        }
//
//        return rawObjects.stream()
//                         .map(obj -> (MessageResponse) obj)
//                         .collect(Collectors.toList());
//    }
//
//    @Override
//    public void saveMessages(Long channelId, List<MessageResponse> messages) {
//        if (messages == null || messages.isEmpty()) return;
//
//        String key = String.format(KEY_PREFIX, channelId);
//        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
//
//        for (MessageResponse msg : messages) {
//            zSetOps.add(key, msg, msg.getId());
//        }
//
//        // Reset TTL
//        redisTemplate.expire(key, CACHE_TTL_MILISECOND, TimeUnit.MILLISECONDS);
//    }
//
//    @Override
//    public void saveMessage(Long channelId, MessageResponse message) {
//        String key = String.format(KEY_PREFIX, channelId);
//
//        // Add new message to ZSet
//        redisTemplate.opsForZSet().add(key, message, message.getId());
//
//        // Reset TTL
//        redisTemplate.expire(key, CACHE_TTL_MILISECOND, TimeUnit.MILLISECONDS);
//    }
//}

