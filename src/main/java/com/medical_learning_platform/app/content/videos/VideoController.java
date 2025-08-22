package com.medical_learning_platform.app.content.videos;


import com.medical_learning_platform.app.content.videos.entity.Video;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/courses")
public class VideoController {

    private final VideoService videoService;

    /**
     * Загрузить видео для урока
     */
    @PostMapping(value = "/{courseId}/sections/{sectionId}/lessons/{lessonId}/video",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<Video> addVideo(
        @PathVariable Long courseId,
        @PathVariable Long sectionId,
        @PathVariable Long lessonId,
        @RequestPart("video") FilePart file,
        Authentication authentication
    ) {
        log.info("Upload video '{}' for course {}, section {}, lesson {}", file.filename(), courseId, sectionId, lessonId);
        Long authorId = Long.parseLong((String) authentication.getPrincipal());
        return videoService.addVideo(courseId, sectionId, lessonId, file, authorId);
    }

    /**
     * Обновить видео урока
     */
    @PutMapping(value = "/{courseId}/sections/{sectionId}/lessons/{lessonId}/video",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<Video> updateVideo(
        @PathVariable Long courseId,
        @PathVariable Long sectionId,
        @PathVariable Long lessonId,
        @NonNull @RequestPart("video") FilePart file,
        Authentication authentication
    ) {
        log.info("Update video '{}' for course {}, section {}, lesson {}", file.filename(), courseId, sectionId, lessonId);
        Long authorId = Long.parseLong((String) authentication.getPrincipal());
        return videoService.updateVideo(courseId, sectionId, lessonId, file, authorId);
    }

    /**
     * Удалить видео урока
     */
    @DeleteMapping("/{courseId}/sections/{sectionId}/lessons/{lessonId}/video/{videoId}")
    public Mono<Void> deleteVideo(
        @PathVariable Long courseId,
        @PathVariable Long sectionId,
        @PathVariable Long lessonId,
        @PathVariable Long videoId,
        Authentication authentication
    ) {
        log.info("Delete video '{}' for course {}, section {}, lesson {}", videoId, courseId, sectionId, lessonId);
        Long authorId = Long.parseLong((String) authentication.getPrincipal());
        return videoService.deleteVideo(courseId, sectionId, lessonId, videoId, authorId);
    }

    /**
     * Получить видео урока
     */
    @GetMapping("/{courseId}/sections/{sectionId}/lessons/{lessonId}/video")
    public Mono<Video> getVideo(
        @PathVariable Long courseId,
        @PathVariable Long sectionId,
        @PathVariable Long lessonId,
        Authentication authentication
    ) {
        Long userId = Long.parseLong((String) authentication.getPrincipal());
        return videoService.getVideo(courseId, sectionId, lessonId, userId);
    }
}

