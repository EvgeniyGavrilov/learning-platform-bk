package com.medical_learning_platform.app.content.courses.repository;

import com.medical_learning_platform.app.content.courses.entity.Course;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CourseRepository extends ReactiveCrudRepository<Course, Long> {
    Flux<Course> findByAuthorId(Long authorId);
}