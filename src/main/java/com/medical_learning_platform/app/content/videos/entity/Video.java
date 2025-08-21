package com.medical_learning_platform.app.content.videos.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("videos")
public class Video {
    @Id
    private Long id;
    private Long lessonId;
    private String filename;
    private String url;
    private LocalDateTime uploadedAt;
}
