package vn.vibeteam.vibe.service.user.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.vibeteam.vibe.dto.request.user.CreateUserProfileRequest;
import vn.vibeteam.vibe.dto.request.user.UpdateUserProfileRequest;
import vn.vibeteam.vibe.dto.response.user.UserProfileResponse;
import vn.vibeteam.vibe.exception.AppException;
import vn.vibeteam.vibe.exception.ErrorCode;
import vn.vibeteam.vibe.model.user.User;
import vn.vibeteam.vibe.model.user.UserProfile;
import vn.vibeteam.vibe.repository.user.UserProfileRepository;
import vn.vibeteam.vibe.repository.user.UserRepository;
import vn.vibeteam.vibe.service.user.UserProfileService;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    @Override
    public UserProfileResponse getUserProfile(Long userId) {
        log.info("Getting user profile for userId: {}", userId);

        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("User profile not found for userId: {}", userId);
                    return new AppException(ErrorCode.USER_NOT_EXISTED);
                });

        log.info("User profile found for userId: {}", userId);
        return mapToResponse(userProfile);
    }

    @Override
    @Transactional
    public UserProfileResponse createUserProfile(Long userId, CreateUserProfileRequest request) {
        log.info("Creating user profile for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with userId: {}", userId);
                    return new AppException(ErrorCode.USER_NOT_EXISTED);
                });

        if (userProfileRepository.findByUserId(userId).isPresent()) {
            log.warn("User profile already exists for userId: {}", userId);
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        UserProfile userProfile = UserProfile.builder()
                .user(user)
                .displayName(request.getDisplayName())
                .dateOfBirth(request.getDateOfBirth())
                .avatarUrl(request.getAvatarUrl())
                .bio(request.getBio())
                .isPublic(request.getIsPublic() != null ? request.getIsPublic() : true)
                .build();

        userProfileRepository.save(userProfile);
        log.info("User profile created successfully for userId: {}", userId);

        return mapToResponse(userProfile);
    }

    @Override
    @Transactional
    public UserProfileResponse updateUserProfile(Long userId, UpdateUserProfileRequest request) {
        log.info("Updating user profile for userId: {}", userId);

        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("User profile not found for userId: {}", userId);
                    return new AppException(ErrorCode.USER_NOT_EXISTED);
                });

        if (request.getDisplayName() != null) {
            userProfile.setDisplayName(request.getDisplayName());
        }
        if (request.getDateOfBirth() != null) {
            userProfile.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getAvatarUrl() != null) {
            userProfile.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getBio() != null) {
            userProfile.setBio(request.getBio());
        }
        if (request.getIsPublic() != null) {
            userProfile.setIsPublic(request.getIsPublic());
        }

        userProfileRepository.save(userProfile);
        log.info("User profile updated successfully for userId: {}", userId);

        return mapToResponse(userProfile);
    }

    private UserProfileResponse mapToResponse(UserProfile userProfile) {
        return UserProfileResponse.builder()
                .id(userProfile.getId())
                .displayName(userProfile.getDisplayName())
                .dateOfBirth(userProfile.getDateOfBirth())
                .avatarUrl(userProfile.getAvatarUrl())
                .bio(userProfile.getBio())
                .isPublic(userProfile.getIsPublic())
                .build();
    }
}

