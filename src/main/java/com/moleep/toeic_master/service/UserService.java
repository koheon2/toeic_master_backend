package com.moleep.toeic_master.service;

import com.moleep.toeic_master.dto.request.ProfileUpdateRequest;
import com.moleep.toeic_master.dto.response.GalleryImageResponse;
import com.moleep.toeic_master.dto.response.StudyResponse;
import com.moleep.toeic_master.dto.response.UserProfileResponse;
import com.moleep.toeic_master.entity.ReviewImage;
import com.moleep.toeic_master.entity.User;
import com.moleep.toeic_master.exception.CustomException;
import com.moleep.toeic_master.repository.ReviewImageRepository;
import com.moleep.toeic_master.repository.StudyMemberRepository;
import com.moleep.toeic_master.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final S3Service s3Service;

    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND));
        return UserProfileResponse.from(user, getProfileImageUrl(user));
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND));
        return UserProfileResponse.from(user, getProfileImageUrl(user));
    }

    private String getProfileImageUrl(User user) {
        return user.getProfileImageKey() != null ? s3Service.getPresignedUrl(user.getProfileImageKey()) : null;
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        if (StringUtils.hasText(request.getNickname()) && !request.getNickname().equals(user.getNickname())) {
            if (userRepository.existsByNickname(request.getNickname())) {
                throw new CustomException("이미 사용중인 닉네임입니다", HttpStatus.BAD_REQUEST);
            }
            user.setNickname(request.getNickname());
        }

        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        if (request.getTendency() != null) {
            user.setTendency(request.getTendency());
        }

        return UserProfileResponse.from(user, getProfileImageUrl(user));
    }

    @Transactional
    public UserProfileResponse updateProfileImage(Long userId, MultipartFile image) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        // 기존 이미지 삭제
        if (user.getProfileImageKey() != null) {
            s3Service.delete(user.getProfileImageKey());
        }

        // 새 이미지 업로드
        String imageKey = s3Service.upload(image, "profile-images");
        user.setProfileImageKey(imageKey);

        return UserProfileResponse.from(user, s3Service.getPresignedUrl(imageKey));
    }

    @Transactional
    public UserProfileResponse deleteProfileImage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        if (user.getProfileImageKey() != null) {
            s3Service.delete(user.getProfileImageKey());
            user.setProfileImageKey(null);
        }

        return UserProfileResponse.from(user, null);
    }

    @Transactional(readOnly = true)
    public Page<GalleryImageResponse> getMyGallery(Long userId, Pageable pageable) {
        return reviewImageRepository.findByUserId(userId, pageable)
                .map(this::toGalleryImageResponse);
    }

    private GalleryImageResponse toGalleryImageResponse(ReviewImage image) {
        return GalleryImageResponse.builder()
                .imageId(image.getId())
                .imageUrl(s3Service.getPresignedUrl(image.getImageKey()))
                .reviewId(image.getReview().getId())
                .schoolId(image.getReview().getSchool().getId())
                .schoolName(image.getReview().getSchool().getName())
                .createdAt(image.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public long getMyGalleryCount(Long userId) {
        return reviewImageRepository.countByUserId(userId);
    }

    @Transactional(readOnly = true)
    public java.util.List<StudyResponse> getMyStudies(Long userId) {
        return studyMemberRepository.findByUserId(userId).stream()
                .map(member -> StudyResponse.from(member.getStudy(), studyMemberRepository.countByStudyId(member.getStudy().getId())))
                .toList();
    }
}
