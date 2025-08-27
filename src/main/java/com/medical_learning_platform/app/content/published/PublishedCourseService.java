package com.medical_learning_platform.app.content.published;

import com.medical_learning_platform.app.content.AccessService;
import com.medical_learning_platform.app.content.courses.CourseService;
import com.medical_learning_platform.app.content.courses.entity.Course;
import com.medical_learning_platform.app.content.courses.entity.CourseAccess;
import com.medical_learning_platform.app.content.courses.repository.CourseAccessRepository;
import com.medical_learning_platform.app.content.courses.repository.CourseRepository;
import com.medical_learning_platform.app.content.published.entity.PublishedCourse;
import com.medical_learning_platform.app.content.published.repository.PublishedCourseRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.time.LocalDateTime;

@Slf4j
@Service
@AllArgsConstructor
public class PublishedCourseService {

    private final PublishedCourseRepository publishedCourseRepository;
    private final CourseAccessRepository courseAccessRepository;
    private final CourseRepository courseRepository;
    private final AccessService accessService;

    public Mono<PublishedCourse> getPublishedCourseOrThrow(Long courseId) {
        return publishedCourseRepository.findByCourseId(courseId)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not published")));
    }

    public Mono<PublishedCourse> publishCourse(Long courseId, Long authorId) {
        return accessService.hasEditAccessOrThrow(courseId, authorId)
            .doOnNext(v -> {log.info("hasEditAccessOrThrow: {}", v);})
            .then(
                courseRepository.findById(courseId)
                    .doOnNext(v -> {log.info("courseRepository.findById: {}", v.toString());})
                    .flatMap(course ->
                        getPublishedCourseOrThrow(courseId)
                            .doOnNext(v -> {log.info("getPublishedCourseOrThrow: {}", v.toString());})
                            .onErrorResume(err  -> {
                                log.info("Move to publisher");
                                PublishedCourse published = new PublishedCourse(null, courseId, authorId, LocalDateTime.now());
                                return publishedCourseRepository.save(published);
                            })
//                isCoursePublished(courseId)
//                    .flatMap(isCoursePublished -> {
//                        if(isCoursePublished) {
//                            return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "Course already published"));
//                        }
//                        PublishedCourse published = new PublishedCourse(null, courseId, LocalDateTime.now());
//                        return publishedCourseRepository.save(published);
//                    }
//                )
                    ).switchIfEmpty(
                        Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"))
                    )
        );
    }

    public Mono<Boolean> isCoursePublished(Long courseId, Long userId) {
        return accessService.hasEditAccessOrThrow(courseId, userId).then(
            courseRepository.findById(courseId).then(
                publishedCourseRepository.existsByCourseId(courseId)
            ).switchIfEmpty(
                Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"))
            )
        );
    }

    public Mono<Boolean> isCoursePublished(Long courseId) {
        return publishedCourseRepository.existsByCourseId(courseId);
    }

    public Flux<Course> allPublishedCourses() {
        return publishedCourseRepository.findAll().flatMap(publishedCourse ->
            courseRepository.findById(
                publishedCourse.getCourseId()
            )
        );
    }
    public Flux<Course> allPublishedCourses(Long authorId) {
        return publishedCourseRepository.findByAuthorId(authorId).flatMapMany(publishedCourse ->
            courseRepository.findById(
                    publishedCourse.getCourseId()
            )
        );
    }

    public Mono<Void> unpublishCourse(Long courseId, Long userId) {
        return accessService.hasEditAccessOrThrow(courseId, userId).then(
            courseRepository.findById(courseId).flatMap(course ->
                publishedCourseRepository.deleteById(courseId)
            ).switchIfEmpty(
                Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"))
            )
        );
    }

    public Mono<Boolean> isUserHaveAccess(Long courseId, Long userId) {
        return accessService.hasReadAccessOrThrow(courseId, userId);
//        return courseAccessRepository.existsByCourseIdAndUserId(courseId, userId);
    }

    public Mono<Boolean> buyCourse(Long courseId, Mono<Principal> principal) {
        return principal
            .map(Principal::getName)
            .map(Long::parseLong)
            .doOnNext(userId -> log.info("User id: {}", userId))
            .flatMap(userId ->
                courseAccessRepository.existsByCourseIdAndUserId(courseId, userId)
                    .flatMap(isExists -> {
                        if (isExists) {
                            return Mono.error(new ResponseStatusException(
                                    HttpStatus.CONFLICT, "Course is already bought"
                            ));
                        }
                        // если доступа нет → создаём новую запись
                        return courseAccessRepository.save(
                                new CourseAccess(null, userId, courseId, LocalDateTime.now(), null)
                        ).thenReturn(true);
                    })
            )
            .switchIfEmpty(Mono.error(new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "User not found"
            )));
    }

}

