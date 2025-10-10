package com.medical_learning_platform.app.content.payment.price;

import com.medical_learning_platform.app.content.payment.price.entity.CoursePrice;
import com.medical_learning_platform.app.content.payment.price.enums.CurrencyCode;
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
    public Mono<BigDecimal> getCoursePrice(Long courseId, CurrencyCode currencyCode) {
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

    /**
     * Добавить цену на курс
     */
    public Mono<CoursePrice> setCoursePrice(CoursePrice coursePrice, Long userId) {

        return coursePriceRepository.save(coursePrice);
    }

    //TODO: ?s
//    public Mono<Boolean> isUserAuthorOfCourse(Long userId, Long courseId) {
//        this.coursePriceRepository.findById(courseId).flatMap(course -> {
//            if (course.)
//        })
//    }
}

