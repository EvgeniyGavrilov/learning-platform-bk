package com.medical_learning_platform.app.content.courses;


import com.medical_learning_platform.app.content.AccessService;
import com.medical_learning_platform.app.content.courses.dto.CourseFullDto;
import com.medical_learning_platform.app.content.lesson.dto.LessonDto;
import com.medical_learning_platform.app.content.lesson.repository.LessonRepository;
import com.medical_learning_platform.app.content.published.PublishedCourseService;
import com.medical_learning_platform.app.content.sections.dto.SectionFullDto;
import com.medical_learning_platform.app.content.courses.entity.Course;
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
    private final AccessService accessService;
    private final PublishedCourseService publishedCourseService;

    /**
     * Проверка наличия курса
     */
    public Mono<Course> getCourseOrThrow(Long courseId) {
        return courseRepository.findById(courseId)
            .switchIfEmpty(Mono.error(new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Course not found"
            )));
    }

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
    public Mono<Course> updateCourse(Long courseId, Course updatedCourse, Long authorId) {
        return getCourseOrThrow(courseId)
            .flatMap(course ->
                this.accessService.hasEditAccessOrThrow(courseId, authorId)
                    .then(Mono.defer(() -> {
                        course.setTitle(updatedCourse.getTitle());
                        course.setDescription(updatedCourse.getDescription());
                        course.setImageUrl(updatedCourse.getImageUrl());
                        return courseRepository.save(course);
                    }))
            );
    }

    /**
     * Удалить курс
     */
    public Mono<Void> deleteCourse(Long courseId, Long authorId) {
        // TODO: Добавить удаление директорий
        return getCourseOrThrow(courseId)
            .flatMap(course ->
                accessService.hasEditAccessOrThrow(courseId, authorId)
                    .then(courseRepository.delete(course))
            );
    }

    /**
     * Получить курс
     */
    public Mono<Course> getCourse(Long courseId, Long userId) {
        return courseRepository.findById(courseId)
            .flatMap(course ->
                publishedCourseService.isCoursePublished(courseId)
                    .flatMap(isPublished -> {
                        if(isPublished) {
                            return Mono.just(course);
                        }

                        return accessService.hasEditAccessOrThrow(courseId, userId).then(
                            Mono.just(course)
                        );
                    })
            )
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found")));
    }

    /**
     * Получить все опубликованных курсы автора
     */
    public Flux<Course> getCoursesByAuthor(Long authorId) {
        return courseRepository.findByAuthorId(authorId);
    }

    /**
     * Получить все курсы автора
     */
    public Flux<Course> getAllPublishedCoursesByAuthor(Long authorId) {
        return publishedCourseService.allPublishedCourses(authorId);
    }
}
