package com.medical_learning_platform.app.content.lesson;

import com.medical_learning_platform.app.content.AccessService;
import com.medical_learning_platform.app.content.courses.CourseService;
import com.medical_learning_platform.app.content.file_loader.VideoFileUtils;
import com.medical_learning_platform.app.content.lesson.entity.Lesson;
import com.medical_learning_platform.app.content.lesson.repository.LessonRepository;
import com.medical_learning_platform.app.content.published.PublishedCourseService;
import com.medical_learning_platform.app.content.sections.SectionService;
import com.medical_learning_platform.app.content.videos.repository.VideoRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.file.Path;
import java.security.Principal;
import java.time.LocalDateTime;

@Slf4j
@Service
@AllArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final VideoRepository videoRepository;

    private final AccessService accessService;
    private final SectionService sectionService;
    private final CourseService courseService;
    private final PublishedCourseService publishedCourseService;


    /**
     * Проверка наличия урока
     */
    public Mono<Lesson> getPublishedOrEditableLessonOrThrow(Lesson lesson, Long courseId, Long userId) {
        return sectionService.isSectionBelongToCourse(lesson.getSectionId(), courseId)
            .flatMap(isSectionAndLessonBelongCourse -> {
                if(isSectionAndLessonBelongCourse) {

                    return publishedCourseService.isCoursePublished(courseId)
                        .flatMap(isPublished -> {
                            if(isPublished) {
                                return lessonRepository.findById(lesson.getId())
                                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found")));
                            }

                            return accessService.hasEditAccessOrThrow(courseId, userId)
                                .then(
                                    lessonRepository.findById(lesson.getId())
                                );
                        });
                }

                return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));
            });


    }

    /**
     * Проверка дсотупа для создания и редактирования урока
     */
    public Mono<Boolean> hasCreateEditLessonAccessOrThrow(Lesson lesson, Long userId) {
        return sectionService.getSectionIfExistOrThrow(lesson.getSectionId())
            .flatMap(section ->
                courseService.getCourseOrThrow(section.getCourseId())
            )
            .flatMap(course ->
                accessService.hasEditAccessOrThrow(course.getId(), userId)
            );
    }

    /**
     * Создать урок (только автор курса)
     */
    public Mono<Lesson> createLesson(Lesson lesson, Long userId) {
        return hasCreateEditLessonAccessOrThrow(lesson, userId)
            .then(
                Mono.defer(() -> {
                    lesson.setCreatedAt(LocalDateTime.now());
                    return lessonRepository.save(lesson);
                })
            );
    }

    /**
     * Обновить урок (только автор курса)
     */
    public Mono<Lesson> updateLesson(Lesson updated, Long userId) {
        return
            hasCreateEditLessonAccessOrThrow(updated, userId)
                .then(Mono.defer(() -> lessonRepository.findById(updated.getId())
                    .flatMap(lesson -> {
                        lesson.setTitle(updated.getTitle());
                        lesson.setDescription(updated.getDescription());
                        lesson.setPosition(updated.getPosition());
                        return lessonRepository.save(lesson);
                    }
                )));

    }

    /**
     * Удалить урок
     */
    public Mono<Void> deleteLesson(Long courseId, Long sectionId, Long lessonId, Long userId) {
        return lessonRepository.findById(lessonId).flatMap(lesson ->
            hasCreateEditLessonAccessOrThrow(lesson, userId)
                .then(
                    videoRepository.findByLessonId(lessonId)
                        .flatMap(video -> {
                            Path videoPath = VideoFileUtils.getLessonDir(courseId, sectionId, lessonId)
                                    .resolve(video.getFilename());
                            return VideoFileUtils.deleteFile(videoPath)
                                    .then(videoRepository.delete(video));
                        })
                        .then(Mono.defer(() ->
                            VideoFileUtils.checkAndDeleteEmptyLessonDir(courseId, sectionId, lessonId)
                        ))
                        .then(lessonRepository.deleteById(lessonId))
                )
        );
    }

    /**
     * Взять урок
     */
    public Mono<Lesson> getLesson(Long lessonId, Long courseId, Long userId) {
        return this.lessonRepository.findById(lessonId).flatMap(lesson -> getPublishedOrEditableLessonOrThrow(lesson, courseId, userId));
    }

    /**
     * Взять уроки
     */
    public Flux<Lesson> getLessons(Long sectionId, Long courseId, Mono<Principal> principal) {
        return publishedCourseService.isCoursePublished(courseId)
            .flatMapMany(isPublished -> {
                if(isPublished) {
                    return lessonRepository.findBySectionIdOrderByPositionAsc(sectionId);
                }

                return principal
                    .map(Principal::getName)
                    .map(Long::parseLong)
                    .flatMapMany(userId ->
                        accessService.hasEditAccessOrThrow(courseId, userId).thenMany(
                            lessonRepository.findBySectionIdOrderByPositionAsc(sectionId)
                        )
                    )
                    .switchIfEmpty(
                        Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Lessons not found"))
                    );
            });

    }
}
