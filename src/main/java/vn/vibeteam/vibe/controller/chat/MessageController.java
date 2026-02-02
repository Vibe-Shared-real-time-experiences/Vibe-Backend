package vn.vibeteam.vibe.controller.chat;

import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import vn.vibeteam.vibe.dto.common.ApiResponse;
import vn.vibeteam.vibe.service.chat.ChatService;
import vn.vibeteam.vibe.util.SecurityUtils;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final ChatService chatService;
    private final SecurityUtils securityUtils;

    @PatchMapping("/{messageId}")
    public ApiResponse<Void> editMessageContent(
            @PathVariable Long messageId,
            @RequestBody String newContent) {

        Long userId = securityUtils.getCurrentUserId();
        chatService.editMessageContent(userId, messageId, newContent);

        return ApiResponse.<Void>builder()
                          .code(200)
                          .message("Message edited successfully")
                          .data(null)
                          .build();
    }

    @DeleteMapping("/{messageId}")
    public ApiResponse<Void> deleteMessage(@PathVariable Long messageId) {

        Long userId = securityUtils.getCurrentUserId();
        chatService.deleteMessage(userId, messageId);

        return ApiResponse.<Void>builder()
                          .code(200)
                          .message("Message deleted successfully")
                          .data(null)
                          .build();
    }
}
