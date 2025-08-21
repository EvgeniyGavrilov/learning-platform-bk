package com.medical_learning_platform.app.content.sections.dto;

import com.medical_learning_platform.app.content.lesson.dto.LessonDto;
import com.medical_learning_platform.app.content.sections.entity.Section;
import com.medical_learning_platform.app.content.videos.dto.VideoDto;
import com.medical_learning_platform.app.content.videos.entity.Video;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SectionFullDto {
    private Long id;
    private Long courseId;
    private String title;
    private int position;
    private List<LessonDto> lessons;

    public SectionFullDto(Section section, List<LessonDto> lessons) {
        this.id = section.getId();
        this.courseId = section.getCourseId();
        this.title = section.getTitle();
        this.position = section.getPosition();
        this.lessons = lessons;
    }
}
