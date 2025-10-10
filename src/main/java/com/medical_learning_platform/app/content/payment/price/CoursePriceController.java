package com.medical_learning_platform.app.content.payment.price;

import com.medical_learning_platform.app.content.payment.price.entity.CoursePrice;
import com.medical_learning_platform.app.content.payment.price.enums.CurrencyCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
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
            @RequestParam("currency") CurrencyCode currency
    ) {
        return coursePriceService.getCoursePrice(courseId, currency);
    }

    /**
     * Получить цену /api/courses/{id}/price?currency=USD
     */
    @PostMapping("/{id}/price")
    public Mono<CoursePrice> setCoursePrice(
            @PathVariable("id") Long courseId,
            @RequestParam("currency") CurrencyCode currency,
            @RequestParam("amount") Long amount,
            @RequestParam("coursePrice") CoursePrice coursePrice,
            Authentication authentication
    ) {
        Long userId = null;
        if (authentication != null && authentication.getPrincipal() != null) {
            try {
                userId = Long.parseLong((String) authentication.getPrincipal());
                return coursePriceService.setCoursePrice(coursePrice, userId);
            } catch (NumberFormatException e) {
                return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
            }
        }
        return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    /**
     * Получить все цены /api/courses/{id}/prices
     */
    @GetMapping("/{id}/prices")
    public Flux<CoursePrice> getAllCoursePrices(@PathVariable("id") Long courseId) {
        return coursePriceService.getAllCoursePrices(courseId);
    }
}

