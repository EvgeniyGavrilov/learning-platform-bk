package com.medical_learning_platform.app.payment.price;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CoursePriceService {

    private final CoursePriceRepository coursePriceRepository;

    /**
     * Получить цену курса в указанной валюте
     */
    public Mono<BigDecimal> getCoursePrice(Long courseId, String currencyCode) {
        return coursePriceRepository.findByCourseIdAndCurrencyCode(courseId, currencyCode)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "Price not found for courseId=" + courseId + " and currency=" + currencyCode
                )))
                .map(CoursePrice::getPrice);
    }

    /**
     * Получить все цены курса во всех валютах
     */
    public Flux<CoursePrice> getAllCoursePrices(Long courseId) {
        return coursePriceRepository.findAllByCourseId(courseId);
    }
}

