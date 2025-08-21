package com.medical_learning_platform.app.content.videos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoDto {
    private Long id;
    private Long lessonId;
    private String filename;
    private String url;
    private LocalDateTime uploadedAt;
}
