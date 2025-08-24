package com.medical_learning_platform.app.content.published.repository;

import com.medical_learning_platform.app.content.published.entity.PublishedCourse;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface PublishedCourseRepository extends ReactiveCrudRepository<PublishedCourse, Long> {
    Mono<Boolean> existsByCourseId(Long courseId);
    Mono<PublishedCourse> findByCourseId(Long courseId);
    Mono<PublishedCourse> findByAuthorId(Long authorId);
}
