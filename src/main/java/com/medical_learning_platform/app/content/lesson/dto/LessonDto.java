package com.medical_learning_platform.app.content.lesson.dto;

import com.medical_learning_platform.app.content.videos.dto.VideoDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LessonDto {
    private Long id;
    private Long sectionId;
    private String title;
    private String description;
    private int position;
    private LocalDateTime createdAt;
//    private List<VideoDto> videos; // вложенные видео
    private VideoDto video; // вложенные видео
}
