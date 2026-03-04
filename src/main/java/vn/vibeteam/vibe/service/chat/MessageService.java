package vn.vibeteam.vibe.service.chat;

import vn.vibeteam.vibe.common.FetchDirection;
import vn.vibeteam.vibe.dto.common.CursorResponse;
import vn.vibeteam.vibe.dto.event.ChannelMessageCreatedEvent;
import vn.vibeteam.vibe.dto.request.chat.CreateMessageRequest;
import vn.vibeteam.vibe.dto.request.chat.MessageContentUpdatedRequest;
import vn.vibeteam.vibe.dto.request.chat.MessageDeletedRequest;
import vn.vibeteam.vibe.dto.response.chat.ChannelHistoryResponse;
import vn.vibeteam.vibe.dto.response.chat.CreateMessageResponse;

import java.util.List;

public interface MessageService {
    CreateMessageResponse sendMessage(Long userId, Long channelId, CreateMessageRequest request);
    CursorResponse<ChannelHistoryResponse> getChannelMessages(Long channelId, Long cursor, FetchDirection direction, int limit);
    void editMessageContent(Long userId, MessageContentUpdatedRequest updateMessageRequest);
    void deleteMessage(Long userId, MessageDeletedRequest messageDeletedRequest);
    void processBatch(List<ChannelMessageCreatedEvent> messages);
}
