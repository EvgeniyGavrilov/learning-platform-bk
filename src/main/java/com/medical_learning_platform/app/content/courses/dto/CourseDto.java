package com.medical_learning_platform.app.content.courses.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseDto {
    private Long id;
    private Long authorId;
    private String title;
    private String description;
    private String imageUrl;
    private LocalDateTime createdAt;
}
