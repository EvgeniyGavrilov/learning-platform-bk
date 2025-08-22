package com.medical_learning_platform.app.content.sections;


import com.medical_learning_platform.app.content.courses.repository.CourseAccessRepository;
import com.medical_learning_platform.app.content.courses.repository.CourseRepository;
import com.medical_learning_platform.app.content.sections.entity.Section;
import com.medical_learning_platform.app.content.sections.repository.SectionRepository;
import com.medical_learning_platform.app.content.videos.repository.VideoRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
@AllArgsConstructor
public class SectionService {

    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final VideoRepository videoRepository;
    private final CourseAccessRepository accessRepository;

    private static final Path UPLOAD_DIR = Paths.get("uploads");

    /**
     * Добавить раздел в курс
     */
    public Mono<Section> addSection(Long courseId, Section section, Long authorId) {
        section.setCourseId(courseId);
        return sectionRepository.save(section);
    }


    public Mono<Section> updateSection(Long courseId, Section section, Long authorId) {
        return sectionRepository.save(section);
    }

    public Mono<Void> deleteSections(Long courseId, Long sectionId, Long authorId) {
        return sectionRepository.deleteById(sectionId);
    }

    public Mono<Section> getSection(Long courseId, Long sectionId, Long userId) {
        return sectionRepository.findById(sectionId);
    }

    public Flux<Section> getSections(Long courseId, Long userId) {
        return sectionRepository.findByCourseIdOrderByPositionAsc(courseId);
    }
}
