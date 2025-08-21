package com.medical_learning_platform.app.content.videos.repository;

import com.medical_learning_platform.app.content.videos.entity.Video;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface VideoRepository extends ReactiveCrudRepository<Video, Long> {
    Mono<Video> findByLessonId(Long lessonId);
}
