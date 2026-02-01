package vn.vibeteam.vibe.service.chat;

import vn.vibeteam.vibe.dto.common.CursorResponse;
import vn.vibeteam.vibe.dto.request.chat.CreateMessageRequest;
import vn.vibeteam.vibe.dto.response.chat.ChannelHistoryResponse;
import vn.vibeteam.vibe.dto.response.chat.CreateMessageResponse;

public interface ChatService {
    CreateMessageResponse sendMessage(Long userId, Long channelId, CreateMessageRequest request);
    CursorResponse<ChannelHistoryResponse> getChannelMessages(Long channelId, Long cursor, int limit);
    void editMessageContent(Long userId, Long messageId, String newContent);
    void deleteMessage(Long userId, Long messageId);
}
