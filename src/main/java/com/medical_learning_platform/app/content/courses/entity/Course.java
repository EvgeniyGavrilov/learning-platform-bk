package com.medical_learning_platform.app.content.courses.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("courses")
public class Course {
    @Id
    private Long id;
    private Long authorId;
    private String title;
    private String description;
    private String imageUrl;
    private LocalDateTime createdAt;
}
