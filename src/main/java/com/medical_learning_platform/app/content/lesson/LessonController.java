package com.medical_learning_platform.app.content.lesson;

import com.medical_learning_platform.app.content.lesson.entity.Lesson;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/courses/{courseId}/sections/{sectionId}/lessons")
public class LessonController {

    private final LessonService lessonService;

    /**
     * Создать урок в секции
     */
    @PostMapping
    public Mono<Lesson> createLesson(
        @PathVariable Long courseId,
        @PathVariable Long sectionId,
        @RequestBody Lesson lesson,
        Authentication authentication
    ) {
        log.info("Create lesson '{}' for course {}, section {}", lesson.getTitle(), courseId, sectionId);
        Long authorId = Long.parseLong((String) authentication.getPrincipal());
        return lessonService.createLesson(lesson, authorId);
    }

    /**
     * Обновить урок
     */
    @PutMapping("/{lessonId}")
    public Mono<Lesson> updateLesson(
        @PathVariable Long courseId,
        @PathVariable Long sectionId,
        @PathVariable Long lessonId,
        @RequestBody Lesson updatedLesson,
        Authentication authentication
    ) {
        log.info("Update lesson {} in course {}, section {}", lessonId, courseId, sectionId);
        Long authorId = Long.parseLong((String) authentication.getPrincipal());
        return lessonService.updateLesson(updatedLesson, authorId);
    }

    /**
     * Удалить урок
     */
    @DeleteMapping("/{lessonId}")
    public Mono<Void> deleteLesson(
        @PathVariable Long courseId,
        @PathVariable Long sectionId,
        @PathVariable Long lessonId,
        Authentication authentication
    ) {
        log.info("Delete lesson {} in course {}, section {}", lessonId, courseId, sectionId);
        Long authorId = Long.parseLong((String) authentication.getPrincipal());
        return lessonService.deleteLesson(courseId, sectionId, lessonId, authorId);
    }

    /**
     * Получить все уроки секции
     */
    @GetMapping
    public Flux<Lesson> getLessons(
        @PathVariable Long courseId,
        @PathVariable Long sectionId,
        Authentication authentication
    ) {
        log.info("Get lessons in course {}, section {}", courseId, sectionId);
        Long userId = Long.parseLong((String) authentication.getPrincipal());
        return lessonService.getLessons(sectionId, courseId, userId);
    }

    /**
     * Получить конкретный урок
     */
    @GetMapping("/{lessonId}")
    public Mono<Lesson> getLesson(
        @PathVariable Long courseId,
        @PathVariable Long sectionId,
        @PathVariable Long lessonId,
        Authentication authentication
    ) {
        log.info("Get lesson {} in course {}, section {}", lessonId, courseId, sectionId);
        Long userId = Long.parseLong((String) authentication.getPrincipal());
        return lessonService.getLesson(lessonId, courseId, userId);
    }
}
