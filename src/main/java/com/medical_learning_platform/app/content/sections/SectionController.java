package com.medical_learning_platform.app.content.sections;


import com.medical_learning_platform.app.content.sections.entity.Section;
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
public class SectionController {

    private final SectionService sectionService;

    /**
     * Добавить раздел в курс
     */
    @PostMapping("/{courseId}/sections")
    public Mono<Section> addSection(
            @PathVariable Long courseId,
            @RequestBody Section section,
            Authentication authentication
    ) {
        log.info("Add section '{}' to course {}", section.getTitle(), courseId);
        Long authorId = Long.parseLong((String) authentication.getPrincipal());
        return sectionService.addSection(courseId, section, authorId);
    }

    /**
     * Добавить раздел в курс
     */
    @PutMapping("/{courseId}/sections")
    public Mono<Section> updateSection(
            @PathVariable Long courseId,
            @RequestBody Section section,
            Authentication authentication
    ) {
        log.info("Add section '{}' to course {}", section.getTitle(), courseId);
        Long authorId = Long.parseLong((String) authentication.getPrincipal());
        return sectionService.updateSection(courseId, section, authorId);
    }

    /**
     * Удалить разделы курса
     */
    @DeleteMapping("/{courseId}/sections/{sectionId}")
    public Mono<Void> deleteSections(
            @PathVariable Long courseId,
            @PathVariable Long sectionId,
            Authentication authentication
    ) {
        log.info("Delete section '{}' to course {}", sectionId, courseId);
        Long authorId = Long.parseLong((String) authentication.getPrincipal());
        return sectionService.deleteSections(courseId, sectionId, authorId);
    }

    /**
     * Получить раздел курса
     */
    @GetMapping("/{courseId}/sections/{sectionId}")
    public Mono<Section> getSection(
            @PathVariable Long courseId,
            @PathVariable Long sectionId,
            Authentication authentication
    ) {
        Long userId = Long.parseLong((String) authentication.getPrincipal());
        return sectionService.getSection(courseId, sectionId, userId);
    }

    /**
     * Получить разделы курса
     */
    @GetMapping("/{courseId}/sections")
    public Flux<Section> getSections(
            @PathVariable Long courseId,
            Authentication authentication
    ) {
        Long userId = Long.parseLong((String) authentication.getPrincipal());
        return sectionService.getSections(courseId, userId);
    }
}

