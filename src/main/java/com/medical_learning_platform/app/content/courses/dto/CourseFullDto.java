package com.medical_learning_platform.app.content.courses.dto;

import com.medical_learning_platform.app.content.sections.dto.SectionFullDto;
import com.medical_learning_platform.app.content.courses.entity.Course;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseFullDto {
    private Long id;
    private Long authorId;
    private String title;
    private String description;
    private String imageUrl;
    private LocalDateTime createdAt;
    private List<SectionFullDto> sections;

    public CourseFullDto(Course course, List<SectionFullDto> sections) {
        this.id = course.getId();
        this.authorId = course.getAuthorId();
        this.title = course.getTitle();
        this.description = course.getDescription();
        this.imageUrl = course.getImageUrl();
        this.createdAt = course.getCreatedAt();
        this.sections = sections;
    }
}
