package com.medical_learning_platform.app.content;

import com.medical_learning_platform.app.content.courses.repository.CourseAccessRepository;
import com.medical_learning_platform.app.content.courses.repository.CourseRepository;
import com.medical_learning_platform.app.content.published.PublishedCourseService;
import com.medical_learning_platform.app.content.published.repository.PublishedCourseRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@AllArgsConstructor
public class AccessService {

    private final CourseRepository courseRepository;
    private final CourseAccessRepository accessRepository;
    private final PublishedCourseRepository publishedCourseRepository;

    public Mono<AccessInfo> getAccessInfo(Long courseId, Long userId) {
        return courseRepository.findById(courseId)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found")))
            .flatMap(course -> {
                boolean isAuthor = course.getAuthorId().equals(userId);
                if (isAuthor) {
                    return Mono.just(new AccessInfo(true, true, true));
                }

                return publishedCourseRepository.existsByCourseId(courseId)
                    .flatMap(isPublished -> {
                        if (isPublished) {
                            return accessRepository.existsByCourseIdAndUserId(courseId, userId)
                                .map(hasAccess ->
                                    new AccessInfo(false, false, hasAccess)
                                );
                        }
                        return Mono.just(new AccessInfo(false, false, false));
                    });
            });
    }

    public Mono<Boolean> hasEditAccessOrThrow(Long courseId, Long authorId) {
        return getAccessInfo(courseId, authorId)
            .flatMap(info -> {
                if (info.isEditAccess()) {
                    return Mono.just(true);
                } else {
                    return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "No permission to edit course"));
                }
            });
    }

    public Mono<Boolean> hasReadAccessOrThrow(Long courseId, Long user) {
        return getAccessInfo(courseId, user)
            .flatMap(info -> {
                if (info.isReadAccess()) {
                    return Mono.just(true);
                } else {
                    return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "No permission to read content"));
                }
            });
    }


}
