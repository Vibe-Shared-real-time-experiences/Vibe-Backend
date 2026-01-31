package vn.vibeteam.vibe.service.chat.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import vn.vibeteam.vibe.common.ChannelType;
import vn.vibeteam.vibe.dto.request.chat.CreateServerRequest;
import vn.vibeteam.vibe.dto.response.chat.CategoryResponse;
import vn.vibeteam.vibe.dto.response.chat.ChannelResponse;
import vn.vibeteam.vibe.dto.response.chat.ServerDetailResponse;
import vn.vibeteam.vibe.dto.response.chat.ServerResponse;
import vn.vibeteam.vibe.exception.AppException;
import vn.vibeteam.vibe.exception.ErrorCode;
import vn.vibeteam.vibe.model.authorization.User;
import vn.vibeteam.vibe.model.server.Category;
import vn.vibeteam.vibe.model.server.Channel;
import vn.vibeteam.vibe.model.server.Server;
import vn.vibeteam.vibe.model.server.ServerMember;
import vn.vibeteam.vibe.repository.chat.ServerMemberRepository;
import vn.vibeteam.vibe.repository.chat.ServerRepository;
import vn.vibeteam.vibe.repository.user.UserRepository;
import vn.vibeteam.vibe.service.chat.ServerService;
import vn.vibeteam.vibe.util.SecurityUtils;

import java.time.LocalDateTime;
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

    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public ServerDetailResponse createServer(CreateServerRequest createServerRequest) {
        log.info("Creating server: {}", createServerRequest.getName());

        // 1. Validate owner exists
        Long ownerId = securityUtils.getCurrentUserId();
        User owner = userRepository.findByIdAndIsActiveTrue(ownerId)
                                   .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        System.out.println("Owner found: " + owner.getId() + owner.getUsername());

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
    public ServerDetailResponse getServerById(long serverId) {
        log.info("Fetching server with ID: {}", serverId);

        Long userId = securityUtils.getCurrentUserId();
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
    public List<ServerResponse> getUserServers() {
        Long userId = securityUtils.getCurrentUserId();
        log.info("Fetching servers for user: {}", userId);

        var userExists = userRepository.findByIdAndIsActiveTrue(userId)
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
    @Transactional
    public void joinServer(long serverId) {
        Long userId = securityUtils.getCurrentUserId();
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
    public void leaveServer(long serverId) {
        Long userId = securityUtils.getCurrentUserId();
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
    public void deleteServer(long serverId) {
        Long userId = securityUtils.getCurrentUserId();
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

    // Create default server with standard settings (include categories and channels)
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
}
