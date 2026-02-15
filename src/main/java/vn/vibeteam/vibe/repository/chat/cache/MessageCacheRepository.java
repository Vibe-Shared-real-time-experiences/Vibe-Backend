package vn.vibeteam.vibe.repository.chat.cache;

import vn.vibeteam.vibe.dto.response.chat.MessageResponse;

import java.util.List;
import java.util.Set;

public interface MessageCacheRepository {
//    List<MessageResponse> getMessages(Long channelId, Long cursor, int limit);
    Set<String> getMessages(Long channelId, Long cursor, int limit);
    void saveMessages(Long channelId, List<MessageResponse> messages);
    void saveMessage(Long channelId, MessageResponse message);
}
