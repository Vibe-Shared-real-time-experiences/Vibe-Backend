package vn.vibeteam.vibe.controller.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import vn.vibeteam.vibe.dto.common.ApiResponse;
import vn.vibeteam.vibe.dto.request.chat.MessageContentUpdatedRequest;
import vn.vibeteam.vibe.dto.request.chat.MessageDeletedRequest;
import vn.vibeteam.vibe.service.chat.MessageService;
import vn.vibeteam.vibe.util.SecurityUtils;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final MessageService messageService;
    private final SecurityUtils securityUtils;

    @PatchMapping("/{messageId}")
    public ApiResponse<Void> editMessageContent(
            @RequestBody MessageContentUpdatedRequest request) {

        Long userId = securityUtils.getCurrentUserId();
        messageService.editMessageContent(userId, request);

        return ApiResponse.<Void>builder()
                          .code(200)
                          .message("Message edited successfully")
                          .data(null)
                          .build();
    }

    @DeleteMapping("/{messageId}")
    public ApiResponse<Void> deleteMessage(@RequestBody MessageDeletedRequest request) {

        Long userId = securityUtils.getCurrentUserId();
        messageService.deleteMessage(userId, request);

        return ApiResponse.<Void>builder()
                          .code(200)
                          .message("Message deleted successfully")
                          .data(null)
                          .build();
    }
}
