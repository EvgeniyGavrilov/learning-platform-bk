package com.medical_learning_platform.app.content.lesson;

import com.medical_learning_platform.app.content.courses.repository.CourseAccessRepository;
import com.medical_learning_platform.app.content.courses.repository.CourseRepository;
import com.medical_learning_platform.app.content.lesson.dto.LessonDto;
import com.medical_learning_platform.app.content.lesson.entity.Lesson;
import com.medical_learning_platform.app.content.lesson.repository.LessonRepository;
import com.medical_learning_platform.app.content.sections.repository.SectionRepository;
import com.medical_learning_platform.app.content.videos.dto.VideoDto;
import com.medical_learning_platform.app.content.videos.entity.Video;
import com.medical_learning_platform.app.content.videos.repository.VideoRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final VideoRepository videoRepository;
    private final SectionRepository sectionRepository;
    private final CourseRepository courseRepository;
    private final CourseAccessRepository accessRepository;

    // Создать урок (только автор курса)
    public Mono<Lesson> createLesson(Lesson lesson, Long userId) {
        return sectionRepository.findById(lesson.getSectionId())
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found")))
            .flatMap(section -> courseRepository.findById(section.getCourseId()))
            .flatMap(course -> {
                if (!course.getAuthorId().equals(userId)) {
                    return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Only author can create lesson"));
                }
                lesson.setCreatedAt(LocalDateTime.now());
                return lessonRepository.save(lesson);
            });
    }

    // Обновить урок (только автор курса)
    public Mono<Lesson> updateLesson(Long lessonId, Lesson updated, Long userId) {
        return lessonRepository.findById(lessonId)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found")))
            .flatMap(lesson -> sectionRepository.findById(lesson.getSectionId())
                .flatMap(section -> courseRepository.findById(section.getCourseId()))
                .flatMap(course -> {
                    if (!course.getAuthorId().equals(userId)) {
                        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Only author can update lesson"));
                    }
                    lesson.setTitle(updated.getTitle());
                    lesson.setDescription(updated.getDescription());
                    lesson.setPosition(updated.getPosition());
                    return lessonRepository.save(lesson);
                })
            );
    }

    // Удалить урок (только автор)
    public Mono<Void> deleteLesson(Long lessonId, Long userId) {
        return lessonRepository.findById(lessonId)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found")))
            .flatMap(lesson -> sectionRepository.findById(lesson.getSectionId())
                .flatMap(section -> courseRepository.findById(section.getCourseId()))
                .flatMap(course -> {
                    if (!course.getAuthorId().equals(userId)) {
                        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Only author can delete lesson"));
                    }
                    return lessonRepository.delete(lesson);
                })
            );
    }

    // Получить уроки секции с видео
    public Flux<LessonDto> getLessonsBySection(Long sectionId, Long userId) {
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
                                            null
                                        )
                                    ))
                            )
                        );
                    })
            );
    }

    public Mono<LessonDto> getLessonById(Long sectionId, Long lessonId, Long userId) {
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
                                    null
                                )))
                            )
                    );
                })
            );
    }
}
