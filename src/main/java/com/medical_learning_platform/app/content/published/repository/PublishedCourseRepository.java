package com.medical_learning_platform.app.content.published.repository;

import com.medical_learning_platform.app.content.published.entity.PublishedCourse;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface PublishedCourseRepository extends ReactiveCrudRepository<PublishedCourse, Long> {
    Mono<Boolean> existsByCourseId(Long courseId);
    Mono<Void> deleteByCourseId(Long courseId);
    Mono<PublishedCourse> findByCourseId(Long courseId);
    Mono<PublishedCourse> findByAuthorId(Long authorId);
    @Query("SELECT * FROM published_courses WHERE (:authorId IS NULL OR author_id <> :authorId)")
    Flux<PublishedCourse> findAllExcludingAuthor(@Param("authorId") Long authorId);

}
