package vn.vibeteam.vibe.service.chat;

import vn.vibeteam.vibe.dto.common.CursorResponse;
import vn.vibeteam.vibe.dto.request.chat.CreateMessageRequest;
import vn.vibeteam.vibe.dto.response.chat.ChannelHistoryResponse;
import vn.vibeteam.vibe.dto.response.chat.CreateMessageResponse;

public interface ChatService {
    CreateMessageResponse sendMessage(long userId, String channelId, CreateMessageRequest request);
    CursorResponse<ChannelHistoryResponse> getChannelMessages(String channelId, String cursor, int limit);
    void editMessageContent(long userId, String messageId, String newContent);
    void deleteMessage(long userId, String messageId);
}
