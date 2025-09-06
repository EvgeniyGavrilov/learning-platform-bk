package com.medical_learning_platform.app.payment.price;

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
    private String code; // USD, EUR, ILS
    private String symbol; // $, €, ₪
    private String name;   // US Dollar, Euro, Israeli Shekel
}

