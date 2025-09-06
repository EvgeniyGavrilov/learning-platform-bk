package com.medical_learning_platform.app.payment.price;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CoursePriceController {

    private final CoursePriceService coursePriceService;

    /**
     * Получить цену /api/courses/{id}/price?currency=USD
     */
    @GetMapping("/{id}/price")
    public Mono<BigDecimal> getCoursePrice(
            @PathVariable("id") Long courseId,
            @RequestParam("currency") String currency
    ) {
        return coursePriceService.getCoursePrice(courseId, currency.toUpperCase());
    }

    /**
     * Получить все цены /api/courses/{id}/prices
     */
    @GetMapping("/{id}/prices")
    public Flux<CoursePrice> getAllCoursePrices(@PathVariable("id") Long courseId) {
        return coursePriceService.getAllCoursePrices(courseId);
    }
}

