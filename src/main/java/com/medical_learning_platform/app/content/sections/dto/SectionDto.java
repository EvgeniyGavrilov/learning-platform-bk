package com.medical_learning_platform.app.content.sections.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SectionDto {
    private Long id;
    private Long courseId;
    private String title;
    private int position;
}
