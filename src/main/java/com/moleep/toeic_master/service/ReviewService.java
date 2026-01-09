package com.moleep.toeic_master.service;

import com.moleep.toeic_master.dto.request.ReviewRequest;
import com.moleep.toeic_master.dto.response.ReviewResponse;
import com.moleep.toeic_master.entity.Review;
import com.moleep.toeic_master.entity.School;
import com.moleep.toeic_master.entity.Tag;
import com.moleep.toeic_master.entity.User;
import com.moleep.toeic_master.exception.CustomException;
import com.moleep.toeic_master.repository.ReviewRepository;
import com.moleep.toeic_master.repository.SchoolRepository;
import com.moleep.toeic_master.repository.TagRepository;
import com.moleep.toeic_master.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsBySchool(Long schoolId, Pageable pageable) {
        return reviewRepository.findBySchoolIdOrderByCreatedAtDesc(schoolId, pageable)
                .map(ReviewResponse::from);
    }

    @Transactional
    public ReviewResponse createReview(Long userId, Long schoolId, ReviewRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new CustomException("학교를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        if (reviewRepository.existsByUserIdAndSchoolId(userId, schoolId)) {
            throw new CustomException("이미 이 학교에 리뷰를 작성했습니다", HttpStatus.BAD_REQUEST);
        }

        Review review = Review.builder()
                .user(user)
                .school(school)
                .rating(request.getRating())
                .content(request.getContent())
                .build();

        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            List<Tag> tags = tagRepository.findByIdIn(request.getTagIds());
            review.setTags(new HashSet<>(tags));
        }

        reviewRepository.save(review);
        school.getReviews().add(review);
        school.updateAvgRating();

        return ReviewResponse.from(review);
    }

    @Transactional
    public ReviewResponse updateReview(Long userId, Long reviewId, ReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException("리뷰를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        if (!review.getUser().getId().equals(userId)) {
            throw new CustomException("수정 권한이 없습니다", HttpStatus.FORBIDDEN);
        }

        review.setRating(request.getRating());
        review.setContent(request.getContent());

        if (request.getTagIds() != null) {
            List<Tag> tags = tagRepository.findByIdIn(request.getTagIds());
            review.setTags(new HashSet<>(tags));
        }

        review.getSchool().updateAvgRating();

        return ReviewResponse.from(review);
    }

    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException("리뷰를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        if (!review.getUser().getId().equals(userId)) {
            throw new CustomException("삭제 권한이 없습니다", HttpStatus.FORBIDDEN);
        }

        School school = review.getSchool();
        school.getReviews().remove(review);
        reviewRepository.delete(review);
        school.updateAvgRating();
    }
}
