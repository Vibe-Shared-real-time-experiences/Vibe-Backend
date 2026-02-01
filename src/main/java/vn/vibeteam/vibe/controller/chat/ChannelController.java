package vn.vibeteam.vibe.controller.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import vn.vibeteam.vibe.dto.common.ApiResponse;
import vn.vibeteam.vibe.dto.request.chat.CreateChannelRequest;
import vn.vibeteam.vibe.dto.response.chat.ChannelResponse;
import vn.vibeteam.vibe.service.chat.ChannelService;
import vn.vibeteam.vibe.util.SecurityUtils;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class ChannelController {

    private final ChannelService channelService;
    private final SecurityUtils securityUtils;

    @PostMapping("/servers/{serverId}/channels")
    public ApiResponse<ChannelResponse> createChannel(
            @PathVariable Long serverId,
            @RequestBody CreateChannelRequest CreateChannelRequest) {

        log.info("Create channel endpoint called");
        Long userId = securityUtils.getCurrentUserId();
        ChannelResponse channelResponse = channelService.createChannel(userId, serverId, CreateChannelRequest);

        return ApiResponse.<ChannelResponse>builder()
                          .code(200)
                          .message("Channel created successfully")
                          .data(channelResponse)
                          .build();
    }

    @GetMapping("/servers/{serverId}/channels")
    public ApiResponse<List<ChannelResponse>> listChannelsByServerId(@PathVariable Long serverId) {

        log.info("List channels by server id endpoint called for serverId: {}", serverId);
        List<ChannelResponse> channels = channelService.listChannelsByServerId(serverId);

        return ApiResponse.<List<ChannelResponse>>builder()
                          .code(200)
                          .message("Channels retrieved successfully")
                          .data(channels)
                          .build();
    }

    @GetMapping("/servers/{serverId}/channels/{channelId}")
    public ApiResponse<ChannelResponse> getChannelById(
            @PathVariable Long serverId,
            @PathVariable Long channelId) {

        log.info("Get channel by id endpoint called for serverId: {}, channelId: {}", serverId, channelId);
        ChannelResponse channelResponse = channelService.getChannelById(serverId, channelId);

        return ApiResponse.<ChannelResponse>builder()
                          .code(200)
                          .message("Channel retrieved successfully")
                          .data(channelResponse)
                          .build();
    }

    @PatchMapping("/channels/{channelId}")
    public String updateChannel(@PathVariable Long channelId) {
        log.info("Update channel endpoint called");
        return "Channel updated";
    }

    @DeleteMapping("/channels/{channelId}")
    public ApiResponse<Void> deleteChannel(
            @PathVariable Long channelId) {

        log.info("Delete channel endpoint called for, channelId: {}", channelId);
        Long userId = securityUtils.getCurrentUserId();
        channelService.deleteChannel(userId, channelId);
        return ApiResponse.<Void>builder()
                          .code(200)
                          .message("Channel deleted successfully")
                          .build();
    }
}
