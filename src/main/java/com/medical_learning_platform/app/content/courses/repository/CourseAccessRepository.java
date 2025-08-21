package com.medical_learning_platform.app.content.courses.repository;

import com.medical_learning_platform.app.content.courses.entity.CourseAccess;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CourseAccessRepository extends ReactiveCrudRepository<CourseAccess, Long> {
    Mono<Boolean> existsByCourseIdAndUserId(Long id, Long userId);
}
