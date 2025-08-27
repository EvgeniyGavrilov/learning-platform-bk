package com.medical_learning_platform.app.content.published;

import com.medical_learning_platform.app.content.courses.entity.Course;
import com.medical_learning_platform.app.content.courses.repository.CourseRepository;
import com.medical_learning_platform.app.content.published.entity.PublishedCourse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.Objects;

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

    @GetMapping("/access/{courseId}")
    public Mono<Boolean> isUserHaveAccess (
        @PathVariable Long courseId,
        Authentication authentication
    ) {
        log.info("Check course access {}", courseId);

        if (authentication == null || authentication.getPrincipal() == null) {
            return Mono.just(false); // пользователь не аутентифицирован
        }

        String principal = authentication.getPrincipal().toString();
        Long userId = Long.parseLong(principal);

        return publishedCourseService.isUserHaveAccess(courseId, userId).onErrorResume(er -> Mono.just(false));
    }

    @PostMapping("/buy/{courseId}")
    public Mono<Boolean> buyCourse (
        @PathVariable Long courseId,
        Authentication authentication,
        @AuthenticationPrincipal Mono<Principal> principal
    ) {
        log.info("Buy course {}", courseId);

        return publishedCourseService.buyCourse(courseId, principal);
    }
}

