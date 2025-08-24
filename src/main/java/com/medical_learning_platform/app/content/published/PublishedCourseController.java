package com.medical_learning_platform.app.content.published;

import com.medical_learning_platform.app.content.courses.entity.Course;
import com.medical_learning_platform.app.content.courses.repository.CourseRepository;
import com.medical_learning_platform.app.content.published.entity.PublishedCourse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/publications")
@AllArgsConstructor
public class PublishedCourseController {

    private final PublishedCourseService publishedCourseService;
    private final CourseRepository courseRepository;

    @PostMapping("/{courseId}")
    public Mono<PublishedCourse> publishCourse (
        @PathVariable Long courseId,
        Authentication authentication
    ) {
        log.info("Publish course: {}", courseId);
        Long userId = Long.parseLong((String) authentication.getPrincipal());
        return publishedCourseService.publishCourse(courseId, userId);
    }

    @DeleteMapping("/{courseId}")
    public Mono<Void> unpublishCourse (
        @PathVariable Long courseId,
        Authentication authentication
    ) {
        log.info("Unpublish course: {}", courseId);
        Long userId = Long.parseLong((String) authentication.getPrincipal());
        return publishedCourseService.unpublishCourse(courseId, userId);
    }

    @GetMapping
    public Flux<Course> allPublishedCourses () {
        log.info("All published courses");
        return publishedCourseService.allPublishedCourses();
    };


    @GetMapping("/is-published/{courseId}")
    public Mono<Boolean> isCoursePublished (
        @PathVariable Long courseId,
        Authentication authentication
    ) {
        log.info("Is courses {} published?", courseId);
        Long userId = Long.parseLong((String) authentication.getPrincipal());
        return publishedCourseService.isCoursePublished(courseId, userId);
    }
}

