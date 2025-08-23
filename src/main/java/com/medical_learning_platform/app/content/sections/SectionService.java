package com.medical_learning_platform.app.content.sections;


import com.medical_learning_platform.app.content.AccessService;
import com.medical_learning_platform.app.content.courses.CourseService;
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
    private final SectionRepository sectionRepository;
    private final AccessService accessService;

    /**
     * Проверка наличия секции
     */
    public Mono<Section> getSectionOrThrow(Long courseId) {
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
                getSectionOrThrow(updatedSection.getId())
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
    public Mono<Void> deleteSections(Long courseId, Long sectionId, Long authorId) {
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
        return getSectionOrThrow(sectionId);
    }

    /**
     * Удалить все разделы в курс
     */
    public Flux<Section> getSections(Long courseId, Long userId) {
        return sectionRepository.findByCourseIdOrderByPositionAsc(courseId);
    }
}
