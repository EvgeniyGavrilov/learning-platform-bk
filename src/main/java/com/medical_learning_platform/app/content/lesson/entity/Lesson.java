package com.medical_learning_platform.app.content.lesson.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("lessons")
public class Lesson {
    @Id
    private Long id;
    private Long sectionId;
    private String title;
    private String description;
    private int position;
    private LocalDateTime createdAt;
}

