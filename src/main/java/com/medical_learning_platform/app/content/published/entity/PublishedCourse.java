package com.medical_learning_platform.app.content.published.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("published_courses")
public class PublishedCourse {
    @Id
    private Long id;
    private Long courseId;
    private Long authorId;
    private LocalDateTime publishedAt;
}

