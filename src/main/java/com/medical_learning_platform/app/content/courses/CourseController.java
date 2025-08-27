package com.medical_learning_platform.app.content.courses;


import com.medical_learning_platform.app.content.courses.entity.Course;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    public Mono<Course> createCourse(@RequestBody Course course, Authentication authentication) {
        log.info("Create course: {}", course.toString());
        Long userId = Long.parseLong((String) authentication.getPrincipal());
        return courseService.createCourse(course, userId);
    }

    @PutMapping("/{id}")
    public Mono<Course> updateCourse(
            @PathVariable Long id,
            @RequestBody Course updatedCourse,
            Authentication authentication
    ) {
        log.info("Update course: {}", updatedCourse.toString());
        Long userId = Long.parseLong((String) authentication.getPrincipal());
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
        log.info("Delete course: {}", id);
        Long authorId = Long.parseLong((String) authentication.getPrincipal());
        return courseService.deleteCourse(id, authorId);
    }

    /**
     * Получить курс по id
     */
    @GetMapping("/{courseId}")
    public Mono<Course> getCourse(
        @PathVariable Long courseId,
        @AuthenticationPrincipal Mono<Principal> principal
    ) {
        log.info("Get course: {}", courseId);
        return courseService.getCourse(courseId, principal);
    }

    /**
     * Получить все курсы автора
     */
    @GetMapping("/author/{authorId}")
    public Flux<Course> getCoursesByAuthor(@PathVariable Long authorId, Authentication authentication) {
        log.info("Get courses by author: {}", authorId);
        Long userId = Long.parseLong((String) authentication.getPrincipal());
        if(authorId.equals(userId)) {
            return courseService.getCoursesByAuthor(authorId);
        }
        return courseService.getAllPublishedCoursesByAuthor(authorId);
    }
}

