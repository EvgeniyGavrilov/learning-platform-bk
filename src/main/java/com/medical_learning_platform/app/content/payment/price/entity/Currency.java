package com.medical_learning_platform.app.content.payment.price.entity;

import com.medical_learning_platform.app.content.payment.price.enums.CurrencyCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("currencies")
public class Currency {
    @Id
    private CurrencyCode code; // USD, EUR, ILS
    private String symbol; // $, €, ₪
    private String name;   // US Dollar, Euro, Israeli Shekel
}

