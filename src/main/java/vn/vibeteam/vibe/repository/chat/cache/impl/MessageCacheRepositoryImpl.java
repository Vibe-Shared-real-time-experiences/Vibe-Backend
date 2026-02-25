package vn.vibeteam.vibe.repository.chat.cache.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;
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
//    private final StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "channel:%d:messages";
    private static final long CACHE_TTL_MILISECOND = 5 * 60 * 1000; // 5 minutes in milliseconds
    private final ObjectMapper objectMapper;

    @Override
    public List<MessageResponse> getMessages(Long channelId, Long cursor, int limit) {
        limit += 1; // Get 1 extra to check if there's more data

        String key = String.format(KEY_PREFIX, channelId);
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();

        // Determine maxScore based on cursor
        double maxScore = (cursor == null)
                ? Double.POSITIVE_INFINITY
                : (cursor - 1);

        // Fetch logic: Fetch from maxScore down to minScore with limit
        Set<Object> rawObjects = zSetOps.reverseRangeByScore(key, Double.NEGATIVE_INFINITY, maxScore, 0, limit);

        if (rawObjects == null || rawObjects.isEmpty()) {
            return Collections.emptyList();
        }

        return rawObjects.stream()
                         .map(obj -> (MessageResponse) obj)
                         .collect(Collectors.toList());
    }

//    @Override
//    public ChannelHistoryResponse getMessages(Long channelId, Long cursor, FetchDirection direction, int limit) {
//        int fetchSize = limit + 1;
//        String key = String.format(KEY_PREFIX, channelId);
//        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
//
//        double maxScore = (cursor == null) ? Double.POSITIVE_INFINITY : (cursor - 1);
//
//        // 1. Get messages with scores <= maxScore in descending order (newest to oldest)
//        // TypedTuple store both score (message_id) + value
//        Set<ZSetOperations.TypedTuple<String>> rawTuples = zSetOps.reverseRangeByScoreWithScores(
//                key, Double.NEGATIVE_INFINITY, maxScore, 0, fetchSize
//        );
//
//        if (rawTuples == null || rawTuples.isEmpty()) {
//            return ChannelHistoryResponse.builder().messages("[]").nextId(null).build();
//        }
//
//        // 2. Find next cursor for pagination
//        List<String> messageStrings = new ArrayList<>();
//        Long nextId = null;
//        int count = 0;
//
//        for (ZSetOperations.TypedTuple<String> tuple : rawTuples) {
//            count++;
//            if (count <= limit) {
//                messageStrings.add(tuple.getValue());
//                nextId = tuple.getScore().longValue();
//            } else {
//                break;
//            }
//        }
//
//        if (rawTuples.size() <= limit) {
//            nextId = null;
//        }
//
//        return mapToChannelHistoryResponse(messageStrings, nextId);
//    }

    @Override
    public void saveMessages(Long channelId, List<MessageResponse> messages) {
        if (messages == null || messages.isEmpty()) return;

        String key = String.format(KEY_PREFIX, channelId);
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();

        for (MessageResponse msg : messages) {
            String json = objectMapper.writeValueAsString(msg);
            Long score = msg.getId();
            zSetOps.add(key, msg, msg.getId());
        }

        // Reset TTL
        redisTemplate.expire(key, CACHE_TTL_MILISECOND, TimeUnit.MILLISECONDS);
    }

    @Override
    public void saveMessage(Long channelId, MessageResponse message) {
        String key = String.format(KEY_PREFIX, channelId);

        // Add new message to ZSet
        redisTemplate.opsForZSet().add(key, message, message.getId());

        String json = objectMapper.writeValueAsString(message);
        Long score = message.getId();
        redisTemplate.opsForZSet().add(key, json, score);

        // Reset TTL
        redisTemplate.expire(key, CACHE_TTL_MILISECOND, TimeUnit.MILLISECONDS);
    }

//    private ChannelHistoryResponse mapToChannelHistoryResponse(List<String> messageResponses, Long nextId) {
//        if (messageResponses == null || messageResponses.isEmpty()) {
//            return ChannelHistoryResponse.builder().messages("[]").nextId(null).build();
//        }
//
//        String messageJsonArray = "[" + String.join(",", messageResponses) + "]";
//
//        return ChannelHistoryResponse.builder()
//                                     .messages(messageJsonArray)
//                                     .nextId(nextId)
//                                     .build();
//    }
}