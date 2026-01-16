package vn.vibeteam.vibe.controller.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.vibeteam.vibe.dto.common.ApiResponse;
import vn.vibeteam.vibe.dto.request.chat.CreateCategoryRequest;
import vn.vibeteam.vibe.service.chat.CategoryService;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("/servers/{serverId}/categories")
    public ApiResponse<Void> createCategory(
            @PathVariable Long serverId,
            @RequestBody CreateCategoryRequest createCategoryRequest) {

        log.info("Create category for server id: {}, category name: {}", serverId, createCategoryRequest.getName());

        categoryService.createCategory(serverId, createCategoryRequest);
        return ApiResponse.<Void>builder()
                          .code(200)
                          .message("Category created successfully")
                          .build();
    }

    @DeleteMapping("/categories/{categoryId}")
    public ApiResponse<Void> deleteCategory(
            @PathVariable Long serverId,
            @PathVariable Long categoryId) {

        log.info("Delete category with id: {} from server id: {}", categoryId, serverId);
        categoryService.deleteCategory(categoryId);

        return ApiResponse.<Void>builder()
                          .code(200)
                          .message("Category deleted successfully")
                          .build();
    }
}
