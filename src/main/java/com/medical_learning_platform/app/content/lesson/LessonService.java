package com.medical_learning_platform.app.content.lesson;

import com.medical_learning_platform.app.content.AccessService;
import com.medical_learning_platform.app.content.courses.CourseService;
import com.medical_learning_platform.app.content.courses.repository.CourseAccessRepository;
import com.medical_learning_platform.app.content.courses.repository.CourseRepository;
import com.medical_learning_platform.app.content.file_loader.VideoFileUtils;
import com.medical_learning_platform.app.content.lesson.dto.LessonDto;
import com.medical_learning_platform.app.content.lesson.entity.Lesson;
import com.medical_learning_platform.app.content.lesson.repository.LessonRepository;
import com.medical_learning_platform.app.content.sections.SectionService;
import com.medical_learning_platform.app.content.sections.repository.SectionRepository;
import com.medical_learning_platform.app.content.videos.dto.VideoDto;
import com.medical_learning_platform.app.content.videos.repository.VideoRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.file.Path;
import java.time.LocalDateTime;

@Slf4j
@Service
@AllArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final VideoRepository videoRepository;
    private final SectionRepository sectionRepository;
    private final CourseRepository courseRepository;
    private final CourseAccessRepository accessRepository;

    private final AccessService accessService;
    private final SectionService sectionService;
    private final CourseService courseService;


    /**
     * Проверка наличия урока
     */
    public Mono<Lesson> getLessonOrThrow(Long lessonId) {
        return lessonRepository.findById(lessonId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found")));
    }

    /**
     * Проверка дсотупа для создания и редактирования урока
     */
    public Mono<Boolean> hasCreateEditLessonAccessOrThrow(Lesson lesson, Long userId) {
        return sectionService.getSectionOrThrow(lesson.getSectionId())
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
    public Mono<Lesson> updateLesson(Long lessonId, Lesson updated, Long userId) {
        return getLessonOrThrow(lessonId).flatMap(lesson ->
            hasCreateEditLessonAccessOrThrow(lesson, userId)
                .then(Mono.defer(() -> {
                    lesson.setTitle(updated.getTitle());
                    lesson.setDescription(updated.getDescription());
                    lesson.setPosition(updated.getPosition());
                    return lessonRepository.save(lesson);
                }))
        );
    }

    /**
     * Удалить урок
     */
    public Mono<Void> deleteLesson(Long courseId, Long sectionId, Long lessonId, Long userId) {
        return getLessonOrThrow(lessonId)
            .flatMap(lesson ->
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
    public Mono<Lesson> getLesson(Long lessonId) {
        return getLessonOrThrow(lessonId);
    }

    /**
     * Взять уроки
     */
    public Flux<Lesson> getLessons(Long sectionId) {
        return lessonRepository.findBySectionIdOrderByPositionAsc(sectionId);
    }

    /**
     * Получить уроки секции с видео
     */
    public Flux<LessonDto> getLessonsWithVideo(Long sectionId, Long userId) { // TODO: no need?
        return sectionRepository.findById(sectionId)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found")))
            .flatMapMany(section ->
                courseRepository.findById(section.getCourseId())
                    .flatMapMany(course -> {
                        boolean isAuthor = course.getAuthorId().equals(userId);
                        Mono<Boolean> hasAccess = isAuthor
                                ? Mono.just(true)
                                : accessRepository.existsByCourseIdAndUserId(course.getId(), userId);

                        return hasAccess.flatMapMany(access -> lessonRepository.findBySectionIdOrderByPositionAsc(sectionId)
                            .flatMap(lesson ->
                                videoRepository.findByLessonId(lesson.getId())
                                    .map(video -> new LessonDto(
                                        lesson.getId(),
                                        lesson.getSectionId(),
                                        lesson.getTitle(),
                                        lesson.getDescription(),
                                        lesson.getPosition(),
                                        lesson.getCreatedAt(),
                                        access ?
                                        new VideoDto(
                                            video.getId(),
                                            video.getLessonId(),
                                            video.getFilename(),
                                            video.getUrl(),
                                            video.getUploadedAt()
                                        ) : null
                                    ))
                                    .switchIfEmpty(Mono.just(
                                        new LessonDto(
                                            lesson.getId(),
                                            lesson.getSectionId(),
                                            lesson.getTitle(),
                                            lesson.getDescription(),
                                            lesson.getPosition(),
                                            lesson.getCreatedAt(),
                                            null
                                        )
                                    ))
                            )
                        );
                    })
            );
    }

    /**
     * Получить урок секции с видео
     */
    public Mono<LessonDto> getLessonWithVideo(Long sectionId, Long lessonId, Long userId) { // TODO: no need?
        return sectionRepository.findById(sectionId)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found")))
            .flatMap(section -> courseRepository.findById(section.getCourseId())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found")))
                .flatMap(course -> {
                    boolean isAuthor = course.getAuthorId().equals(userId);
                    Mono<Boolean> hasAccess = isAuthor
                        ? Mono.just(true)
                        : accessRepository.existsByCourseIdAndUserId(course.getId(), userId);

                    return hasAccess.flatMap(access ->
                        lessonRepository.findById(lessonId)
                            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found")))
                            .flatMap(lesson -> videoRepository.findByLessonId(lesson.getId())
                                .map(video -> new LessonDto(
                                    lesson.getId(),
                                    lesson.getSectionId(),
                                    lesson.getTitle(),
                                    lesson.getDescription(),
                                    lesson.getPosition(),
                                    lesson.getCreatedAt(),
                                    access ? new VideoDto(
                                            video.getId(),
                                            video.getLessonId(),
                                            video.getFilename(),
                                            video.getUrl(),
                                            video.getUploadedAt()
                                    ) : null
                                ))
                                .switchIfEmpty(Mono.just(new LessonDto(
                                    lesson.getId(),
                                    lesson.getSectionId(),
                                    lesson.getTitle(),
                                    lesson.getDescription(),
                                    lesson.getPosition(),
                                    lesson.getCreatedAt(),
                                    null
                                )))
                            )
                    );
                })
            );
    }
}
