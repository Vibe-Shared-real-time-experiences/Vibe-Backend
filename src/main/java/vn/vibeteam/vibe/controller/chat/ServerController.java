package vn.vibeteam.vibe.controller.chat;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import vn.vibeteam.vibe.dto.common.ApiResponse;
import vn.vibeteam.vibe.dto.request.chat.CreateCategoryRequest;
import vn.vibeteam.vibe.dto.request.chat.CreateChannelRequest;
import vn.vibeteam.vibe.dto.request.chat.CreateServerRequest;
import vn.vibeteam.vibe.dto.response.chat.ChannelResponse;
import vn.vibeteam.vibe.dto.response.chat.ChannelUnreadResponse;
import vn.vibeteam.vibe.dto.response.chat.ServerDetailResponse;
import vn.vibeteam.vibe.dto.response.chat.ServerResponse;
import vn.vibeteam.vibe.dto.response.user.UserReadStateResponse;
import vn.vibeteam.vibe.service.chat.CategoryService;
import vn.vibeteam.vibe.service.chat.ChannelService;
import vn.vibeteam.vibe.service.chat.ServerService;
import vn.vibeteam.vibe.util.SecurityUtils;

import java.util.List;

@RestController
@RequestMapping("/api/v1/servers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Server-Controller", description = "APIs for creating, updating, and retrieving servers")
public class ServerController {

    private final ServerService serverService;
    private final CategoryService categoryService;
    private final ChannelService channelService;
    private final SecurityUtils securityUtils;

    @PostMapping("")
    @Operation(
            summary = "Create a new Server",
            description = "Creates a new server and assigns the creator as the owner. Returns the created server details."
    )
    @ApiResponses(value = {
            // Success (201 Created)
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Server created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ServerDetailResponse.class)
                    )
            ),
            // Error (400 Bad Request)
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid input (e.g., name is empty)",
                    content = @Content
            ),
            // Error (401 Unauthorized)
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "User is not authenticated",
                    content = @Content
            )
    })
    public ApiResponse<ServerDetailResponse> createServer(@RequestBody CreateServerRequest createServerRequest) {
        log.info("Create server endpoint called, server name: {}", createServerRequest.getName());

        Long userId = securityUtils.getCurrentUserId();
        ServerDetailResponse response = serverService.createServer(userId, createServerRequest);

        return ApiResponse.<ServerDetailResponse>builder()
                          .code(200)
                          .message("Server created successfully")
                          .data(response)
                          .build();
    }

    @PostMapping("/{serverId}/categories")
    public ApiResponse<Void> createCategory(
            @PathVariable Long serverId,
            @RequestBody CreateCategoryRequest createCategoryRequest) {

        log.info("Create category for server id: {}, category name: {}", serverId, createCategoryRequest.getName());

        Long userId = securityUtils.getCurrentUserId();
        categoryService.createCategory(userId, serverId, createCategoryRequest);
        return ApiResponse.<Void>builder()
                          .code(200)
                          .message("Category created successfully")
                          .build();
    }

    @PostMapping("/{serverId}/channels")
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

    @GetMapping("/{serverId}/channels")
    public ApiResponse<List<ChannelResponse>> listChannelsByServerId(@PathVariable Long serverId) {

        log.info("List channels by server id endpoint called for serverId: {}", serverId);
        List<ChannelResponse> channels = channelService.listChannelsByServerId(serverId);

        return ApiResponse.<List<ChannelResponse>>builder()
                          .code(200)
                          .message("Channels retrieved successfully")
                          .data(channels)
                          .build();
    }

    @GetMapping("/{serverId}/channels/{channelId}")
    public ApiResponse<ChannelResponse> getChannelsByServerId(
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

    @PostMapping("/{serverId}/join")
    public ApiResponse<Void> joinServer(@PathVariable Long serverId) {
        log.info("Join server endpoint called for server: {}", serverId);

        Long userId = securityUtils.getCurrentUserId();
        serverService.joinServer(userId, serverId);

        return ApiResponse.<Void>builder()
                          .code(200)
                          .message("Joined server successfully")
                          .build();
    }

    @PostMapping("/{serverId}/leave")
    public ApiResponse<Void> leaveServer(@PathVariable Long serverId) {
        log.info("Leave server endpoint called for server: {}", serverId);

        Long userId = securityUtils.getCurrentUserId();
        serverService.leaveServer(userId, serverId);

        return ApiResponse.<Void>builder()
                          .code(200)
                          .message("Left server successfully")
                          .build();
    }

    @GetMapping("")
    public ApiResponse<List<ServerResponse>> listServers() {
        log.info("List servers endpoint called");

        Long userId = securityUtils.getCurrentUserId();
        List<ServerResponse> response = serverService.getUserServers(userId);

        return ApiResponse.<List<ServerResponse>>builder()
                          .code(200)
                          .message("Servers retrieved successfully")
                          .data(response)
                          .build();
    }

    @GetMapping("/{serverId}")
    public ApiResponse<ServerDetailResponse> getServer(@PathVariable Long serverId) {
        log.info("Get server endpoint called for server: {}", serverId);

        Long userId = securityUtils.getCurrentUserId();
        ServerDetailResponse response = serverService.getServerById(userId, serverId);

        return ApiResponse.<ServerDetailResponse>builder()
                          .code(200)
                          .message("Server retrieved successfully")
                          .data(response)
                          .build();
    }

    @GetMapping("/{serverId}/read-states")
    public ApiResponse<List<ChannelUnreadResponse>> getUserReadStateOnServer(@PathVariable Long serverId) {
        Long userId = securityUtils.getCurrentUserId();
        List<ChannelUnreadResponse> channelUnreadResponses = serverService.getUserReadStateInServer(userId, serverId);

        return ApiResponse.<List<ChannelUnreadResponse>>builder()
                          .code(200)
                          .message("Channel read states retrieved successfully")
                          .data(channelUnreadResponses)
                          .build();
    }

    @DeleteMapping("/{serverId}")
    public ApiResponse<Void> deleteServer(@PathVariable Long serverId) {
        log.info("Delete server endpoint called for server: {}", serverId);

        Long userId = securityUtils.getCurrentUserId();
        serverService.deleteServer(userId, serverId);

        return ApiResponse.<Void>builder()
                          .code(200)
                          .message("Server deleted successfully")
                          .build();
    }
}
