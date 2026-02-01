package vn.vibeteam.vibe.controller.chat;

import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import vn.vibeteam.vibe.dto.common.ApiResponse;
import vn.vibeteam.vibe.dto.common.CursorResponse;
import vn.vibeteam.vibe.dto.request.chat.CreateMessageRequest;
import vn.vibeteam.vibe.dto.response.chat.ChannelHistoryResponse;
import vn.vibeteam.vibe.dto.response.chat.CreateMessageResponse;
import vn.vibeteam.vibe.service.chat.ChatService;
import vn.vibeteam.vibe.util.SecurityUtils;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final ChatService chatService;
    private final SecurityUtils securityUtils;

    @GetMapping("/channels/{channelId}/messages")
    public ApiResponse<CursorResponse<ChannelHistoryResponse>> getChannelMessages(
            @PathVariable String channelId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "50")
            @Max(value = 50, message = "The  'limit' parameter cannot exceed 50.") int limit) {

        log.info("Fetching messages for channelId: {}, cursor: {}, limit: {}", channelId, cursor, limit);
        CursorResponse<ChannelHistoryResponse> channelHistoryResponse = chatService.getChannelMessages(channelId,
                                                                                                         cursor, limit);

        return ApiResponse.<CursorResponse<ChannelHistoryResponse>>builder()
                          .code(200)
                          .message("Channel message history retrieved successfully")
                          .data(channelHistoryResponse)
                          .build();
    }

    @PostMapping("/channels/{channelId}/messages")
    public ApiResponse<CreateMessageResponse> sendMessage(
            @PathVariable String channelId,
            @RequestBody CreateMessageRequest request) {

        Long userId = securityUtils.getCurrentUserId();
        CreateMessageResponse response = chatService.sendMessage(userId, channelId, request);

        return ApiResponse.<CreateMessageResponse>builder()
                          .code(200)
                          .message("Message sent successfully")
                          .data(response)
                          .build();
    }

    @PatchMapping("/messages/{messageId}")
    public ApiResponse<Void> editMessageContent(
            @PathVariable String messageId,
            @RequestBody String newContent) {

        Long userId = securityUtils.getCurrentUserId();
        chatService.editMessageContent(userId, messageId, newContent);

        return ApiResponse.<Void>builder()
                          .code(200)
                          .message("Message edited successfully")
                          .data(null)
                          .build();
    }

    @DeleteMapping("/messages/{messageId}")
    public ApiResponse<Void> deleteMessage(@PathVariable String messageId) {

        Long userId = securityUtils.getCurrentUserId();
        chatService.deleteMessage(userId, messageId);

        return ApiResponse.<Void>builder()
                          .code(200)
                          .message("Message deleted successfully")
                          .data(null)
                          .build();
    }
}
