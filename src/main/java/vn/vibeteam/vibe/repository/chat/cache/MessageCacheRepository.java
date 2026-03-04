package vn.vibeteam.vibe.repository.chat.cache;

import vn.vibeteam.vibe.common.FetchDirection;
import vn.vibeteam.vibe.dto.response.chat.MessageResponse;

import java.util.List;

public interface MessageCacheRepository {
    List<MessageResponse> getMessages(Long channelId, Long cursor, FetchDirection direction, int limit);
    void saveMessages(Long channelId, List<MessageResponse> messages);
    void saveMessage(Long channelId, MessageResponse message);
    void updateMessageContent(Long channelId, Long messageId, String newContent);
    void deleteMessage(Long channelId, Long messageId);
}
