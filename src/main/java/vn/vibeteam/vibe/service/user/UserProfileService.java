package vn.vibeteam.vibe.service.user;

import vn.vibeteam.vibe.dto.request.user.CreateUserProfileRequest;
import vn.vibeteam.vibe.dto.request.user.UpdateUserProfileRequest;
import vn.vibeteam.vibe.dto.response.user.UserProfileResponse;

public interface UserProfileService {
    UserProfileResponse getUserProfile(Long userId);
    UserProfileResponse createUserProfile(Long userId, CreateUserProfileRequest request);
    UserProfileResponse updateUserProfile(Long userId, UpdateUserProfileRequest request);
}

