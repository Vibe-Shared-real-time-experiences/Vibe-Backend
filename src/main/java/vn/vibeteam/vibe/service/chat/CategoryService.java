package vn.vibeteam.vibe.service.chat;

import vn.vibeteam.vibe.dto.request.chat.CreateCategoryRequest;
import vn.vibeteam.vibe.dto.request.chat.CreateServerRequest;
import vn.vibeteam.vibe.dto.response.chat.ServerDetailResponse;
import vn.vibeteam.vibe.dto.response.chat.ServerResponse;

import java.util.List;

public interface CategoryService {
    void createCategory(Long userId, Long serverId, CreateCategoryRequest createCategoryRequest);
    void deleteCategory(Long userId, Long categoryId);
}
