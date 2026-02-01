package vn.vibeteam.vibe.controller.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import vn.vibeteam.vibe.dto.common.ApiResponse;
import vn.vibeteam.vibe.service.chat.CategoryService;
import vn.vibeteam.vibe.util.SecurityUtils;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;
    private final SecurityUtils securityUtils;

    @DeleteMapping("/{categoryId}")
    public ApiResponse<Void> deleteCategory(
            @PathVariable Long categoryId) {

        log.info("Delete category with id: {}", categoryId);

        Long userId = securityUtils.getCurrentUserId();
        categoryService.deleteCategory(userId, categoryId);

        return ApiResponse.<Void>builder()
                          .code(200)
                          .message("Category deleted successfully")
                          .build();
    }
}
