package com.medical_learning_platform.app.content.payment.price.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("course_prices")
public class CoursePrice {
    @Id
    private Long id;
    private Long courseId;
    private String countryCode;  // US, DE, IL
    private String currencyCode; // FK to currencies
    private BigDecimal price;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
}

