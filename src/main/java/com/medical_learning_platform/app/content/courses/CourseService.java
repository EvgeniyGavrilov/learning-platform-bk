package com.medical_learning_platform.app.content.courses;


import com.medical_learning_platform.app.content.courses.dto.CourseFullDto;
import com.medical_learning_platform.app.content.lesson.dto.LessonDto;
import com.medical_learning_platform.app.content.lesson.repository.LessonRepository;
import com.medical_learning_platform.app.content.sections.dto.SectionFullDto;
import com.medical_learning_platform.app.content.courses.entity.Course;
import com.medical_learning_platform.app.content.courses.repository.CourseAccessRepository;
import com.medical_learning_platform.app.content.courses.repository.CourseRepository;
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

import java.time.LocalDateTime;

@Slf4j
@Service
@AllArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final VideoRepository videoRepository;
    private final CourseAccessRepository accessRepository;
    private final LessonRepository lessonRepository;

    /**
     * Создать курс
     */
    public Mono<Course> createCourse(Course course, Long authorId) {
        course.setAuthorId(authorId);
        course.setCreatedAt(LocalDateTime.now());
        return courseRepository.save(course);
    }

    /**
     * Обновить курс
     */
    public Mono<Course> updateCourse(Long id, Course updatedCourse, Long authorId) {
        return courseRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found")))
            .flatMap(course -> {
                if (!course.getAuthorId().equals(authorId)) {
                    return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your course"));
                }
                course.setTitle(updatedCourse.getTitle());
                course.setDescription(updatedCourse.getDescription());
                course.setImageUrl(updatedCourse.getImageUrl());
                return courseRepository.save(course);
            });
    }

    /**
     * Удалить курс
     */
    public Mono<Void> deleteCourse(Long id, Long authorId) {
        return courseRepository.findById(id)
            .flatMap(course -> {
                if (!course.getAuthorId().equals(authorId)) {
                    return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your course"));
                }
                return courseRepository.delete(course);
            });
    }

    /**
     * Получить курс
     */
    public Mono<Course> getCourse(Long courseId, Long userId) {
        return courseRepository.findById(courseId)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found")));
    }

    /**
     * Получить все курсы автора
     */
    public Flux<Course> getCoursesByAuthor(Long authorId) {
        return courseRepository.findByAuthorId(authorId);
    }

    /**
     * Получить структуру курса (курс + разделы + видео)
     */
    public Mono<CourseFullDto> getFullCourse(Long courseId, Long userId) {
        return getCourse(courseId, userId) // проверка доступа к курсу
            .flatMap(course ->
                sectionRepository.findByCourseIdOrderByPositionAsc(courseId)
                    .flatMap(section ->
                        // получаем уроки секции
                        lessonRepository.findBySectionIdOrderByPositionAsc(section.getId())
                            .flatMap(lesson ->
                                // получаем видео урока (1 видео на урок)
                                videoRepository.findByLessonId(lesson.getId())
                                    .map(video -> {
                                        VideoDto videoDto = new VideoDto(
                                            video.getId(),
                                            video.getLessonId(),
                                            video.getFilename(),
                                            video.getUrl(),
                                            video.getUploadedAt()
                                        );
                                        return new LessonDto(
                                            lesson.getId(),
                                            lesson.getSectionId(),
                                            lesson.getTitle(),
                                            lesson.getDescription(),
                                            lesson.getPosition(),
                                            lesson.getCreatedAt(),
                                            videoDto
                                        );
                                    })
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
                            .collectList()
                            .map(lessons -> new SectionFullDto(section, lessons))
                    )
                    .collectList()
                    .map(sections -> new CourseFullDto(course, sections))
            );
    }

    private Mono<Boolean> hasAccess(Course course, Long userId) {
        if (course.getAuthorId().equals(userId)) {
            return Mono.just(true);
        }
        return accessRepository.existsByCourseIdAndUserId(course.getId(), userId);
    }
}
