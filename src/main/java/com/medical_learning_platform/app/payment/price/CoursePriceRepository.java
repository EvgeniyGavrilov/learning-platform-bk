package com.medical_learning_platform.app.payment.price;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CoursePriceRepository extends ReactiveCrudRepository<CoursePrice, Long> {
    Mono<CoursePrice> findByCourseIdAndCurrencyCode(Long courseId, String currencyCode);
    Flux<CoursePrice> findAllByCourseId(Long courseId);
}

