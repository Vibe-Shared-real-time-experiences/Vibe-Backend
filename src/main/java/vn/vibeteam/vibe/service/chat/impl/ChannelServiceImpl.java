package vn.vibeteam.vibe.service.chat.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import vn.vibeteam.vibe.common.ChannelType;
import vn.vibeteam.vibe.dto.request.chat.CreateChannelRequest;
import vn.vibeteam.vibe.dto.response.chat.ChannelResponse;
import vn.vibeteam.vibe.exception.AppException;
import vn.vibeteam.vibe.exception.ErrorCode;
import vn.vibeteam.vibe.model.server.Category;
import vn.vibeteam.vibe.model.server.Channel;
import vn.vibeteam.vibe.model.server.Server;
import vn.vibeteam.vibe.repository.chat.CategoryRepository;
import vn.vibeteam.vibe.repository.chat.ChannelRepository;
import vn.vibeteam.vibe.repository.chat.ServerRepository;
import vn.vibeteam.vibe.service.chat.ChannelService;
import vn.vibeteam.vibe.util.SecurityUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChannelServiceImpl implements ChannelService {

    private final ChannelRepository channelRepository;
    private final ServerRepository serverRepository;
    private final CategoryRepository categoryRepository;

    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public ChannelResponse createChannel(long serverId, CreateChannelRequest createChannelRequest) {
        log.info("Creating channel in server: {}", serverId);

        // 1. Verify server exists and is not deleted
        Server server = serverRepository.findById(serverId)
                                        .orElseThrow(() -> new AppException(ErrorCode.SERVER_NOT_FOUND));

        // 2. Verify user is the server owner
        boolean isOwner = isOwner(server.getOwner().getId());
        if (!isOwner) {
            log.warn("User {} attempted to create channel in server {} without ownership",
                     securityUtils.getCurrentUserId(), serverId);
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // 3. Verify category exists if provided
        Category category = null;
        if (createChannelRequest.getCategoryId() != null && !createChannelRequest.getCategoryId().isEmpty()) {
            Long categoryId = Long.parseLong(createChannelRequest.getCategoryId());
            category = categoryRepository.findById(categoryId)
                                         .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
            // Verify category belongs to this server
            if (!category.getServer().getId().equals(serverId)) {
                log.warn("Category {} does not belong to server {}", categoryId, serverId);
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
        }

        System.out.println("Category: " + category);

        // 4. Create channel
        int position = 0;
        if (category != null) {
            position = category.getChannels() != null ? category.getChannels().size() : 0;
        }
        Channel channel = Channel.builder()
                                 .server(server)
                                 .category(category)
                                 .name(createChannelRequest.getName())
                                 .type(createChannelRequest.getType() !=
                                       null ? createChannelRequest.getType() : ChannelType.TEXT)
                                 .position(position)
                                 .isPublic(createChannelRequest.getPublicAccess() !=
                                           null ? createChannelRequest.getPublicAccess() : true)
                                 .isActive(true)
                                 .build();

        Channel savedChannel = channelRepository.save(channel);
        log.info("Channel {} created successfully with ID: {}", createChannelRequest.getName(), savedChannel.getId());

        return mapToChannelResponse(savedChannel);
    }

    private boolean isOwner(Long ownerId) {
        Long currentUserId = securityUtils.getCurrentUserId();
        return ownerId.equals(currentUserId);
    }

    @Override
    public List<ChannelResponse> listChannelsByServerId(long serverId) {
        log.info("Listing channels for server: {}", serverId);

        // Verify server exists
        serverRepository.findById(serverId)
                        .orElseThrow(() -> new AppException(ErrorCode.SERVER_NOT_FOUND));

        List<ChannelResponse> channels = channelRepository.findByServerId(serverId)
                                                          .stream()
                                                          .map(this::mapToChannelResponse)
                                                          .collect(Collectors.toList());

        log.info("Retrieved {} channels for server: {}", channels.size(), serverId);
        return channels;
    }

    @Override
    public ChannelResponse getChannelById(long serverId, long channelId) {
        log.info("Fetching channel {} from server {}", channelId, serverId);

        // 1. Verify server exists
        serverRepository.findById(serverId)
                        .orElseThrow(() -> new AppException(ErrorCode.SERVER_NOT_FOUND));

        // 2. Verify channel exists and belongs to the server
        Channel channel = channelRepository.findById(channelId)
                                           .orElseThrow(() -> new AppException(ErrorCode.CHANNEL_NOT_FOUND));

        if (!channel.getServer().getId().equals(serverId)) {
            log.warn("Channel {} does not belong to server {}", channelId, serverId);
            throw new AppException(ErrorCode.CHANNEL_NOT_FOUND);
        }

        log.info("Channel {} fetched successfully", channelId);
        return mapToChannelResponse(channel);
    }

    @Override
    @Transactional
    public void deleteChannel(long channelId) {
        log.info("Deleting channel: {}", channelId);

        // 1. Verify channel exists and is not deleted
        Channel channel = channelRepository.findById(channelId)
                                           .orElseThrow(() -> new AppException(ErrorCode.CHANNEL_NOT_FOUND));

        // 2. Verify server exists and is not deleted
        Server server = serverRepository.findById(channel.getServer().getId())
                                        .orElseThrow(() -> new AppException(ErrorCode.SERVER_NOT_FOUND));

        // 3. Verify user is the server owner
        boolean isOwner = isOwner(server.getOwner().getId());
        if (!isOwner) {
            log.warn("User {} attempted to delete channel {} without server ownership",
                     securityUtils.getCurrentUserId(), channelId);
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // 4. Soft delete the channel
        channelRepository.deleteChannelById(channelId);
        log.info("Channel {} deleted successfully", channelId);
    }

    private ChannelResponse mapToChannelResponse(Channel channel) {
        return ChannelResponse.builder()
                              .id(channel.getId())
                              .serverId(channel.getServer().getId())
                              .categoryId(channel.getCategory() != null ? channel.getCategory().getId() : null)
                              .name(channel.getName())
                              .type(channel.getType())
                              .position(channel.getPosition() != null ? channel.getPosition() : 0)
                              .build();
    }
}
