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
import vn.vibeteam.vibe.service.chat.ChannelService;
import vn.vibeteam.vibe.service.chat.ChatService;
import vn.vibeteam.vibe.util.SecurityUtils;

import java.util.List;

@RestController
@RequestMapping("/api/v1/channels")
@RequiredArgsConstructor
@Slf4j
public class ChannelController {

    private final ChannelService channelService;
    private final ChatService chatService;
    private final SecurityUtils securityUtils;

    @GetMapping("/{channelId}/messages")
    public ApiResponse<CursorResponse<ChannelHistoryResponse>> getChannelMessages(
            @PathVariable Long channelId,
            @RequestParam(required = false) Long cursor,
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

    @PostMapping("/{channelId}/messages")
    public ApiResponse<CreateMessageResponse> sendMessage(
            @PathVariable Long channelId,
            @RequestBody CreateMessageRequest request) {

        long userId = securityUtils.getCurrentUserId();
        CreateMessageResponse response = chatService.sendMessage(userId, channelId, request);

        return ApiResponse.<CreateMessageResponse>builder()
                          .code(200)
                          .message("Message sent successfully")
                          .data(response)
                          .build();
    }

    @PatchMapping("/{channelId}")
    public String updateChannel(@PathVariable Long channelId) {
        log.info("Update channel endpoint called");
        return "Channel updated";
    }

    @DeleteMapping("/{channelId}")
    public ApiResponse<Void> deleteChannel(
            @PathVariable Long channelId) {

        log.info("Delete channel endpoint called for, channelId: {}", channelId);
        long userId = securityUtils.getCurrentUserId();
        channelService.deleteChannel(userId, channelId);
        return ApiResponse.<Void>builder()
                          .code(200)
                          .message("Channel deleted successfully")
                          .build();
    }
}
