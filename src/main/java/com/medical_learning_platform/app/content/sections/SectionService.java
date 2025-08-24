package com.medical_learning_platform.app.content.sections;


import com.medical_learning_platform.app.content.AccessService;
import com.medical_learning_platform.app.content.courses.CourseService;
import com.medical_learning_platform.app.content.courses.repository.CourseRepository;
import com.medical_learning_platform.app.content.published.PublishedCourseService;
import com.medical_learning_platform.app.content.sections.entity.Section;
import com.medical_learning_platform.app.content.sections.repository.SectionRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Slf4j
@Service
@AllArgsConstructor
public class SectionService {

    private final CourseService courseService;
    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final AccessService accessService;
    private final PublishedCourseService publishedCourseService;

    /**
     * Проверка наличия секции
     */
    public Mono<Section> getSectionIfExistOrThrow(Long courseId) {
        return sectionRepository.findById(courseId)
            .switchIfEmpty(Mono.error(new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Section not found"
            )));
    }

    /**
     * Добавить раздел в курс
     */
    public Mono<Section> addSection(Long courseId, Section section, Long authorId) {
        return courseService.getCourseOrThrow(courseId)
            .then(
                this.accessService.hasEditAccessOrThrow(courseId, authorId)
                    .then(Mono.defer(() -> {
                        section.setCourseId(courseId);
                        return sectionRepository.save(section);
                    }))
            );
    }

    /**
     * Обновить раздел в курс
     */
    public Mono<Section> updateSection(Long courseId, Section updatedSection, Long authorId) {
        return courseService.getCourseOrThrow(courseId)
            .then(
                getSectionIfExistOrThrow(updatedSection.getId())
                    .flatMap(section ->
                        this.accessService.hasEditAccessOrThrow(courseId, authorId)
                            .then(Mono.defer(() -> {
                                section.setCourseId(courseId);
                                section.setTitle(updatedSection.getTitle());
                                section.setPosition(updatedSection.getPosition());
                                return sectionRepository.save(section);
                            }))
                    )
            );
    }

    /**
     * Удалить раздел в курс
     */
    public Mono<Void> deleteSections(Long courseId, Long sectionId, Long authorId) { // TODO: don't delete dir's
        return courseService.getCourseOrThrow(courseId)
            .then(
                this.accessService.hasEditAccessOrThrow(courseId, authorId)
                    .then(Mono.defer(() -> sectionRepository.deleteById(sectionId)))
            );

    }

    /**
     * Получить раздел в курс
     */
    public Mono<Section> getSection(Long courseId, Long sectionId, Long userId) {
        return getSectionIfExistOrThrow(sectionId)
            .flatMap(section -> this.isSectionBelongToCourse(section.getId(), courseId)
                .flatMap(isBelong -> {
                    if(isBelong) {
                        publishedCourseService.isCoursePublished(courseId)
                            .flatMap(isPublished -> {
                                if(isPublished) {
                                    return Mono.just(section);
                                }

                                return accessService.hasEditAccessOrThrow(courseId, userId)
                                    .then(Mono.just(section));
                            });
                    }
                    return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found"));
                }));
    }

    /**
     * Удалить все разделы в курс
     */
    public Flux<Section> getSections(Long courseId, Long userId) {
        return publishedCourseService.isCoursePublished(courseId)
            .flatMapMany(isPublished -> {
                if(isPublished) {
                    return sectionRepository.findByCourseIdOrderByPositionAsc(courseId);
                }

                return accessService.hasEditAccessOrThrow(courseId, userId)
                    .thenMany(
                        sectionRepository.findByCourseIdOrderByPositionAsc(courseId)
                    );
            });
    }

    public Mono<Boolean> isSectionBelongToCourse(Long sectionId, Long courseId) {
        return courseRepository.findById(sectionId).flatMap(course -> Mono.just(course.getId().equals(courseId)));
    }
}
