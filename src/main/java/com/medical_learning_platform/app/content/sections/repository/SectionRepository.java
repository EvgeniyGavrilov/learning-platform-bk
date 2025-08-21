package com.medical_learning_platform.app.content.sections.repository;

import com.medical_learning_platform.app.content.sections.entity.Section;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface SectionRepository extends ReactiveCrudRepository<Section, Long> {
    Flux<Section> findByCourseIdOrderByPositionAsc(Long courseId);
}
