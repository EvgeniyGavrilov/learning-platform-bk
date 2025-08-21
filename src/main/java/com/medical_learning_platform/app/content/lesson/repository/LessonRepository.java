package com.medical_learning_platform.app.content.lesson.repository;

import com.medical_learning_platform.app.content.lesson.entity.Lesson;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface LessonRepository extends ReactiveCrudRepository<Lesson, Long> {
    Flux<Lesson> findBySectionIdOrderByPositionAsc(Long sectionId);
    Mono<Lesson> findByIdAndSectionId(Long id, Long sectionId);
    Mono<Void> deleteByIdAndSectionId(Long id, Long sectionId);
}
