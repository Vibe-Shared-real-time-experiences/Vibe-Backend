package vn.vibeteam.vibe.repository.chat.cache;

import vn.vibeteam.vibe.common.FetchDirection;
import vn.vibeteam.vibe.dto.response.chat.MessageResponse;

import java.util.List;

public interface MessageCacheRepository {
    List<MessageResponse> getMessages(Long channelId, Long cursor, FetchDirection direction, int limit);
//    Set<String> getMessages(Long channelId, Long cursor, int limit);
//    ChannelHistoryResponse getMessages(Long channelId, Long cursor, FetchDirection direction, int limit);
    void saveMessages(Long channelId, List<MessageResponse> messages);
    void saveMessage(Long channelId, MessageResponse message);
}
