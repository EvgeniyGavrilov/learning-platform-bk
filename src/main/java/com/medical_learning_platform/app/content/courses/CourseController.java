package com.medical_learning_platform.app.content.courses;


import com.medical_learning_platform.app.content.courses.dto.CourseFullDto;
import com.medical_learning_platform.app.content.courses.entity.Course;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    public Mono<Course> createCourse(@RequestBody Course course, Authentication authentication) {
        log.info("Create course: {}", course.getTitle());
        Long userId = (Long) authentication.getPrincipal();
        return courseService.createCourse(course, userId);
    }

    @PutMapping("/{id}")
    public Mono<Course> updateCourse(
            @PathVariable Long id,
            @RequestBody Course updatedCourse,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return courseService.updateCourse(id, updatedCourse, userId);
    }

    /**
     * Удалить курс по id
     */
    @DeleteMapping("/{id}")
    public Mono<Void> deleteCourse(
        @PathVariable Long id,
        Authentication authentication
    ) {
        Long authorId = (Long) authentication.getPrincipal();
        return courseService.deleteCourse(id, authorId);
    }

    /**
     * Получить курс по id
     */
    @GetMapping("/{courseId}")
    public Mono<Course> getCourse(@PathVariable Long courseId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return courseService.getCourse(courseId, userId);
    }

    /**
     * Получить все курсы автора
     */
    @GetMapping("/author/{authorId}")
    public Flux<Course> getCoursesByAuthor(@PathVariable Long authorId) {
        return courseService.getCoursesByAuthor(authorId);
    }

    /**
     * Получить структуру курса целиком
     */
    @GetMapping("/{courseId}/full")
    public Mono<CourseFullDto> getFullCourse(@PathVariable Long courseId, Authentication authentication) {
        Long authorId = (Long) authentication.getPrincipal();
        return courseService.getFullCourse(courseId, authorId);
    }
}

