package vn.vibeteam.vibe.controller.chat;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.vibeteam.vibe.dto.common.ApiResponse;
import vn.vibeteam.vibe.dto.request.chat.CreateServerRequest;
import vn.vibeteam.vibe.dto.response.chat.ServerDetailResponse;
import vn.vibeteam.vibe.dto.response.chat.ServerResponse;
import vn.vibeteam.vibe.service.chat.ServerService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/servers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Server-Controller", description = "APIs for creating, updating, and retrieving servers")
public class ServerController {

    private final ServerService serverService;

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

        ServerDetailResponse response = serverService.createServer(createServerRequest);

        return ApiResponse.<ServerDetailResponse>builder()
                          .code(200)
                          .message("Server created successfully")
                          .data(response)
                          .build();
    }

    @PostMapping("/{serverId}/join")
    public ApiResponse<Void> joinServer(@PathVariable Long serverId) {
        log.info("Join server endpoint called for server: {}", serverId);

        serverService.joinServer(serverId);

        return ApiResponse.<Void>builder()
                          .code(200)
                          .message("Joined server successfully")
                          .build();
    }

    @PostMapping("/{serverId}/leave")
    public ApiResponse<Void> leaveServer(@PathVariable Long serverId) {
        log.info("Leave server endpoint called for server: {}", serverId);

        serverService.leaveServer(serverId);

        return ApiResponse.<Void>builder()
                          .code(200)
                          .message("Left server successfully")
                          .build();
    }

    @GetMapping("")
    public ApiResponse<List<ServerResponse>> listServers() {
        log.info("List servers endpoint called");

        List<ServerResponse> response = serverService.getUserServers();

        return ApiResponse.<List<ServerResponse>>builder()
                          .code(200)
                          .message("Servers retrieved successfully")
                          .data(response)
                          .build();
    }

    @GetMapping("/{serverId}")
    public ApiResponse<ServerDetailResponse> getServer(@PathVariable Long serverId) {
        log.info("Get server endpoint called for server: {}", serverId);

        ServerDetailResponse response = serverService.getServerById(serverId);

        return ApiResponse.<ServerDetailResponse>builder()
                          .code(200)
                          .message("Server retrieved successfully")
                          .data(response)
                          .build();
    }

    @DeleteMapping("/{serverId}")
    public ApiResponse<Void> deleteServer(@PathVariable Long serverId) {
        log.info("Delete server endpoint called for server: {}", serverId);

        serverService.deleteServer(serverId);

        return ApiResponse.<Void>builder()
                          .code(200)
                          .message("Server deleted successfully")
                          .build();
    }
}
