package com.medical_learning_platform.app.auth.code;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CodeRepository extends ReactiveCrudRepository<Code, Long> {
    Mono<Code> findByEmail(String email);
    Mono<Void> deleteByEmail(String email);
}
