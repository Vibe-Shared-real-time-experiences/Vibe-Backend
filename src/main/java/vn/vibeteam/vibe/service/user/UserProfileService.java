package vn.vibeteam.vibe.service.user;

import vn.vibeteam.vibe.dto.request.user.CreateUserProfileRequest;
import vn.vibeteam.vibe.dto.request.user.UpdateUserProfileRequest;
import vn.vibeteam.vibe.dto.request.user.UserSummaryRequest;
import vn.vibeteam.vibe.dto.response.user.UserProfileResponse;
import vn.vibeteam.vibe.dto.response.user.UserSummaryResponse;

import java.util.List;

public interface UserProfileService {
    UserProfileResponse getUserProfile(Long userId);
    List<UserSummaryResponse> getUserSummaryInfo(UserSummaryRequest request);
    UserProfileResponse createUserProfile(Long userId, CreateUserProfileRequest request);
    UserProfileResponse updateUserProfile(Long userId, UpdateUserProfileRequest request);
}

