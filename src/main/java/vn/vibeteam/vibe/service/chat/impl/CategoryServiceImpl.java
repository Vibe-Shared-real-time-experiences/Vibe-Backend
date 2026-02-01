package vn.vibeteam.vibe.service.chat.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.vibeteam.vibe.dto.request.chat.CreateCategoryRequest;
import vn.vibeteam.vibe.exception.AppException;
import vn.vibeteam.vibe.exception.ErrorCode;
import vn.vibeteam.vibe.model.server.Category;
import vn.vibeteam.vibe.model.server.Server;
import vn.vibeteam.vibe.repository.chat.CategoryRepository;
import vn.vibeteam.vibe.repository.chat.ServerRepository;
import vn.vibeteam.vibe.service.chat.CategoryService;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ServerRepository serverRepository;

    @Override
    @Transactional
    public void createCategory(Long userId, Long serverId, CreateCategoryRequest createCategoryRequest) {
        log.info("Creating category in server: {}", serverId);

        // 1. Verify server exists and is not deleted
        Server server = serverRepository.findById(serverId)
                                        .orElseThrow(() -> new AppException(ErrorCode.SERVER_NOT_FOUND));

        // 2. Verify user is the server owner
        boolean isOwner = isOwner(userId, server.getOwner().getId());
        if (!isOwner) {
            log.warn("User {} attempted to create category in server {} without ownership", userId, serverId);
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // 3. Create category
        Category category = Category.builder()
                                    .server(server)
                                    .name(createCategoryRequest.getName())
                                    .isPublic(createCategoryRequest.getPublicAccess() != null ?
                                                      createCategoryRequest.getPublicAccess() : true)
                                    .isActive(true)
                                    .build();

        categoryRepository.save(category);
        log.info("Category {} created successfully in server {}", createCategoryRequest.getName(), serverId);
    }

    @Override
    @Transactional
    public void deleteCategory(Long userId, Long categoryId) {
        log.info("Deleting category: {}", categoryId);

        // 1. Verify category exists and is not deleted
        Category category = categoryRepository.findById(categoryId)
                                              .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        // 2. Verify server exists and is not deleted
        Server server = serverRepository.findById(category.getServer().getId())
                                        .orElseThrow(() -> new AppException(ErrorCode.SERVER_NOT_FOUND));

        // 3. Verify user is the server owner
        boolean isOwner = isOwner(userId, server.getOwner().getId());
        if (!isOwner) {
            log.warn("User {} attempted to delete category {} in server {} without ownership",
                     userId, categoryId, server.getId());
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // 4. Soft delete category
        categoryRepository.deleteCategoryById(categoryId);
        log.info("Category {} deleted successfully", categoryId);
    }

    private boolean isOwner(Long userId, Long ownerId) {
        return Objects.equals(userId, ownerId);
    }
}
