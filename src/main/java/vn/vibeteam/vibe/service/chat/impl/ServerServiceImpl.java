package vn.vibeteam.vibe.service.chat.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.vibeteam.vibe.common.ChannelType;
import vn.vibeteam.vibe.dto.request.chat.CreateServerRequest;
import vn.vibeteam.vibe.dto.response.chat.*;
import vn.vibeteam.vibe.exception.AppException;
import vn.vibeteam.vibe.exception.ErrorCode;
import vn.vibeteam.vibe.model.user.User;
import vn.vibeteam.vibe.model.server.Category;
import vn.vibeteam.vibe.model.channel.Channel;
import vn.vibeteam.vibe.model.server.Server;
import vn.vibeteam.vibe.model.server.ServerMember;
import vn.vibeteam.vibe.repository.chat.ChannelRepository;
import vn.vibeteam.vibe.repository.chat.ServerMemberRepository;
import vn.vibeteam.vibe.repository.chat.ServerRepository;
import vn.vibeteam.vibe.repository.user.UserRepository;
import vn.vibeteam.vibe.service.chat.ServerService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServerServiceImpl implements ServerService {

    private final ServerRepository serverRepository;
    private final ServerMemberRepository serverMemberRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;

    @Override
    @Transactional
    public ServerDetailResponse createServer(Long userId, CreateServerRequest createServerRequest) {
        log.info("Creating server: {}", createServerRequest.getName());

        // 1. Validate owner exists
        User owner = userRepository.findByIdAndIsActiveTrue(userId)
                                   .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // 2. Create default server
        Server defaultServer = createDefaultServer(owner, createServerRequest);

        // 3. Add owner as first member
        ServerMember ownerMember = ServerMember.builder()
                                               .server(defaultServer)
                                               .user(owner)
                                               .nickname(owner.getUsername())
                                               .joinedAt(LocalDateTime.now())
                                               .isActive(true)
                                               .build();

        defaultServer.addMember(ownerMember);
        Server savedServer = serverRepository.save(defaultServer);

        log.info("Server {} created successfully with ID: {}", createServerRequest.getName(), savedServer.getId());

        return mapToServerDetailResponse(savedServer);
    }

    @Override
    public ServerDetailResponse getServerById(Long userId, Long serverId) {
        log.info("Fetching server with ID: {}", serverId);

        Server server = serverRepository.findServerDetailById(serverId)
                                        .orElseThrow(() -> new AppException(ErrorCode.SERVER_NOT_FOUND));
        ServerMember serverMember = serverMemberRepository.findByServerIdAndUserId(serverId, userId)
                                                          .orElse(null);

        // If server is private, check if user is a member
        if (server.getIsPublic() == Boolean.FALSE && serverMember == null) {
            log.warn("Attempted to access private server: {}", serverId);
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        log.info("Server {} fetched successfully", serverId);
        return mapToServerDetailResponse(server);
    }

    @Override
    public List<ServerResponse> getUserServers(Long userId) {
        log.info("Fetching servers for user: {}", userId);

        userRepository.findByIdAndIsActiveTrue(userId)
                      .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        List<ServerResponse> servers = serverMemberRepository.findByUserId(userId)
                                                             .stream()
                                                             .map(serverMember -> mapToServerResponse(
                                                                     serverMember.getServer()))
                                                             .collect(Collectors.toList());

        log.info("Retrieved {} servers for user: {}", servers.size(), userId);
        return servers;
    }

    @Override
    public List<ChannelUnreadResponse> getUserReadStateInServer(Long userId, Long serverId) {
        log.info("Fetching read states for user {} in server {}", userId, serverId);

        List<ChannelUnreadResponse> channelUnreadResponses = new ArrayList<>();

        // 1. TODO: Check if user is a member of the server

        // 2. Get all unread states for channels in the server
        List<ChannelRepository.ChannelUnreadProjection> channelUnreadStates =
                channelRepository.getChannelUnreadStates(serverId, userId);

        channelUnreadStates.forEach(projection -> {
            boolean isUnread = projection.getLastMessageId() != null &&
                               !projection.getLastMessageId().equals(projection.getLastReadMessageId());
            ChannelUnreadResponse response = ChannelUnreadResponse.builder()
                                                                  .channelId(projection.getChannelId())
                                                                  .lastMessageId(projection.getLastMessageId())
                                                                  .lastReadMessageId(projection.getLastReadMessageId())
                                                                  .unread(isUnread)
                                                                  .build();
            channelUnreadResponses.add(response);
        });

        log.info("Retrieved read states for {} channels in server {}", channelUnreadResponses.size(), serverId);
        return channelUnreadResponses;
    }

    @Override
    @Transactional
    public void joinServer(Long userId, Long serverId) {
        log.info("User {} attempting to join server {}", userId, serverId);

        Server server = serverRepository.findById(serverId)
                                        .orElseThrow(() -> new AppException(ErrorCode.SERVER_NOT_FOUND));

        // Check if server is public
        if (!server.getIsPublic()) {
            log.warn("User {} cannot join private server {}", userId, serverId);
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        User user = userRepository.findByIdAndIsActiveTrue(userId)
                                  .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        ServerMember member = serverMemberRepository.findByServerIdAndUserId(serverId, userId)
                                                    .orElse(null);

        // New member joining the server
        if (member == null) {
            ServerMember serverMember = ServerMember.builder()
                                                    .server(server)
                                                    .user(user)
                                                    .nickname(user.getUsername())
                                                    .joinedAt(LocalDateTime.now())
                                                    .isActive(true)
                                                    .build();

            serverMemberRepository.save(serverMember);
            log.info("User {} successfully joined server {}", userId, serverId);

            return;
        }

        // Member had joined this server before
        boolean isJoined = member.getIsActive();
        if (!isJoined) {
            member.setIsActive(true);
            member.setJoinedAt(LocalDateTime.now());
            serverMemberRepository.save(member);
            log.info("User {} re-joined server {}", userId, serverId);
        }
    }

    @Override
    @Transactional
    public void leaveServer(Long userId, Long serverId) {
        log.info("User {} attempting to leave server {}", userId, serverId);

        Server server = serverRepository.findById(serverId)
                                        .orElseThrow(() -> new AppException(ErrorCode.SERVER_NOT_FOUND));

        // Check if user is owner
        if (server.getOwner().getId().equals(userId)) {
            log.warn("Server owner {} cannot leave server {}", userId, serverId);
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        boolean isExisted = serverMemberRepository.existsByServerIdAndUserId(serverId, userId);
        if (!isExisted) {
            log.warn("User {} is not a member of server {}", userId, serverId);
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        serverMemberRepository.deleteServerMemberByMemberId(serverId, userId);
        log.info("User {} successfully left server {}", userId, serverId);
    }

    @Override
    @Transactional
    public void deleteServer(Long userId, Long serverId) {
        log.info("User {} attempting to delete server {}", userId, serverId);

        Server server = serverRepository.findById(serverId)
                                        .orElseThrow(() -> new AppException(ErrorCode.SERVER_NOT_FOUND));

        // Check if user is owner
        if (!server.getOwner().getId().equals(userId)) {
            log.warn("User {} is not the owner of server {}", userId, serverId);
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        serverRepository.deleteServerById(serverId);
        log.info("Server {} successfully deleted by user {}", serverId, userId);
    }

    private ServerResponse mapToServerResponse(Server server) {
        return ServerResponse.builder()
                             .id(server.getId())
                             .ownerId(server.getOwner().getId())
                             .name(server.getName())
                             .description(server.getDescription())
                             .iconUrl(server.getIconUrl())
                             .publicAccess(server.getIsPublic())
                             .active(server.getIsActive())
                             .build();
    }

    private ServerDetailResponse mapToServerDetailResponse(Server server) {
        // Sort categories by position
        List<CategoryResponse> categoryResponses = server.getCategories()
                                                         .stream()
                                                         .sorted(Comparator.comparingInt(Category::getPosition))
                                                         .map(this::mapToCategoryResponse)
                                                         .toList();

        return ServerDetailResponse.builder()
                                   .id(server.getId())
                                   .ownerId(server.getOwner().getId())
                                   .name(server.getName())
                                   .description(server.getDescription())
                                   .iconUrl(server.getIconUrl())
                                   .publicAccess(server.getIsPublic())
                                   .active(server.getIsActive())
                                   .categories(categoryResponses)
                                   .build();
    }

    private CategoryResponse mapToCategoryResponse(Category category) {
        List<ChannelResponse> channelResponses = category.getChannels()
                                                         .stream()
                                                         .sorted(Comparator.comparingInt(Channel::getPosition))
                                                         .map(this::mapToChannelResponse)
                                                         .toList();

        return CategoryResponse.builder()
                               .id(category.getId())
                               .serverId(category.getId())
                               .name(category.getName())
                               .position(category.getPosition())
                               .publicAccess(category.getIsPublic())
                               .active(category.getIsActive())
                               .channels(channelResponses)
                               .build();
    }

    private ChannelResponse mapToChannelResponse(Channel channel) {
        return ChannelResponse.builder()
                              .id(channel.getId())
                              .serverId(channel.getServer().getId())
                              .categoryId(channel.getCategory() != null ? channel.getCategory().getId() : null)
                              .name(channel.getName())
                              .type(channel.getType())
                              .position(channel.getPosition())
                              .build();
    }

    private Server createDefaultServer(User owner, CreateServerRequest createServerRequest) {
        Server defaultServer = Server.builder()
                                     .owner(owner)
                                     .name(createServerRequest.getName())
                                     .description(createServerRequest.getDescription())
                                     .iconUrl(createServerRequest.getIconUrl())
                                     .isPublic(createServerRequest.getPublicAccess() !=
                                               null ? createServerRequest.getPublicAccess() : true)
                                     .isActive(true)
                                     .build();

        Category defaultTextCategory = Category.builder()
                                               .name("Text channels")
                                               .position(0)
                                               .isPublic(true)
                                               .isActive(true)
                                               .build();

        Category defaultVoiceCategory = Category.builder()
                                                .name("Voice Channels")
                                                .position(1)
                                                .isPublic(true)
                                                .isActive(true)
                                                .build();

        Channel generalTextChannel = Channel.builder()
                                            .name("General")
                                            .type(ChannelType.TEXT)
                                            .position(0)
                                            .isPublic(true)
                                            .isActive(true)
                                            .build();

        Channel generalVoiceChannel = Channel.builder()
                                             .name("General")
                                             .type(ChannelType.VOICE)
                                             .position(0)
                                             .isPublic(true)
                                             .isActive(true)
                                             .build();

        defaultServer.addCategory(defaultTextCategory);
        defaultServer.addCategory(defaultVoiceCategory);

        defaultServer.addChannel(generalTextChannel);
        defaultServer.addChannel(generalVoiceChannel);

        defaultTextCategory.addChannel(generalTextChannel);
        defaultVoiceCategory.addChannel(generalVoiceChannel);

        return defaultServer;
    }
}
