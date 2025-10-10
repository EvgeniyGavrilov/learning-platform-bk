package com.medical_learning_platform.app.content.payment.price;

import com.medical_learning_platform.app.content.payment.price.entity.CoursePrice;
import com.medical_learning_platform.app.content.payment.price.enums.CurrencyCode;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CoursePriceRepository extends ReactiveCrudRepository<CoursePrice, Long> {
    Mono<CoursePrice> findByCourseIdAndCurrencyCode(Long courseId, CurrencyCode currencyCode);
    Flux<CoursePrice> findAllByCourseId(Long courseId);
}

