package vn.vibeteam.vibe.worker;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import vn.vibeteam.vibe.dto.event.ChannelMessageCreatedEvent;
import vn.vibeteam.vibe.service.chat.MessageService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageSyncWorker {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final MessageService messageService;

    private static final String STREAM_KEY = "vibe:stream:messages";
    private static final String CONSUMER_GROUP = "vibe-consumers";
    private static final String CONSUMER_NAME = "worker-1";
    private static final int BATCH_SIZE = 100;

    @PostConstruct
    public void init() {
        log.info("Initializing MessageStreamWorker");
        try {
            redisTemplate.opsForStream().createGroup(STREAM_KEY, CONSUMER_GROUP);
        } catch (Exception e) {
            log.info("Consumer Group ready or already exists");
        }
    }

    // Polling & Consuming Stream every 500ms
    @Scheduled(fixedDelay = 500)
    @Transactional
    public void consumeStream() {
        log.info("Consumer Group: {} consume", CONSUMER_GROUP);

        try {
            // 1. Read message form redis Stream (XREADGROUP)
            List<MapRecord<String, Object, Object>> messages = redisTemplate.opsForStream().read(
                    Consumer.from(CONSUMER_GROUP, CONSUMER_NAME),
                    StreamReadOptions.empty().count(BATCH_SIZE).block(Duration.ofSeconds(2000)),
                    StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed())
            );

            if (messages == null || messages.isEmpty()) return;

            List<ChannelMessageCreatedEvent> batchEntities = new ArrayList<>();
            List<RecordId> recordIds = new ArrayList<>();

            // 2. Deserialize & Prepare Batch
            for (MapRecord<String, Object, Object> record : messages) {
                String json = (String) record.getValue().get("payload");
                if (json != null) {
                    ChannelMessageCreatedEvent channelMessage = objectMapper.readValue(json, ChannelMessageCreatedEvent.class);
                    batchEntities.add(channelMessage);
                }

                recordIds.add(record.getId());
            }

            // 3. Delegate to Service for Batch Processing
            if (!batchEntities.isEmpty()) {
                messageService.processBatch(batchEntities);
            }

            // 4. ACK (XACK)
            redisTemplate.opsForStream().acknowledge(STREAM_KEY, CONSUMER_GROUP, recordIds.toArray(new RecordId[0]));

            // 5. Clean up (Optional)
            redisTemplate.opsForStream().delete(STREAM_KEY, recordIds.toArray(new RecordId[0]));

            log.info("Processed and ACKed {} messages", batchEntities.size());
        } catch (Exception e) {
            log.error("Error consuming stream", e);
            // No ACK here for retry
            // TODO: Handle poison message scenario
        }
    }
}