package com.medical_learning_platform.app.content.videos;


import com.medical_learning_platform.app.content.courses.repository.CourseAccessRepository;
import com.medical_learning_platform.app.content.courses.repository.CourseRepository;
import com.medical_learning_platform.app.content.lesson.entity.Lesson;
import com.medical_learning_platform.app.content.lesson.repository.LessonRepository;
import com.medical_learning_platform.app.content.sections.repository.SectionRepository;
import com.medical_learning_platform.app.content.videos.entity.Video;
import com.medical_learning_platform.app.content.videos.repository.VideoRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Slf4j
@Service
@AllArgsConstructor
public class VideoService {

    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final VideoRepository videoRepository;
    private final LessonRepository lessonRepository;

    private static final Path UPLOAD_DIR = Paths.get("uploads");

    /**
     * Загрузить видео в раздел
     */
    public Mono<Video> addVideo(Long courseId, Long sectionId, Long lessonId, FilePart file, Long authorId) {
        return validateAuthor(courseId, sectionId, lessonId, authorId)
            .flatMap(lesson -> {
                String filename = file.filename();

                Path sectionDir = UPLOAD_DIR
                    .resolve("course_" + courseId)
                    .resolve("section_" + sectionId)
                    .resolve("lesson_" + lessonId);

                try {
                    Files.createDirectories(sectionDir);
                } catch (IOException e) {
                    return Mono.error(new RuntimeException("Failed to create section directory", e));
                }

                Path destination = sectionDir.resolve(filename);

                return file.transferTo(destination)
                    .then(Mono.defer(() -> {
                        Video video = new Video();
                        video.setLessonId(lessonId);
                        video.setFilename(filename);
                        video.setUrl("/api/video/uploaded/" + courseId + "/" + sectionId +"/" + lessonId + "/" + filename);
                        video.setUploadedAt(LocalDateTime.now());
                        return videoRepository.save(video);
                    }));
            });
    }

    /**
     * Обновить видео в раздел
     */
    public Mono<Video> updateVideo(Long courseId, Long sectionId, Long lessonId, FilePart file, Long authorId) {
        return validateAuthor(courseId, sectionId, lessonId, authorId)
            .flatMap(lesson -> videoRepository.findByLessonId(lesson.getId())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Video not found")))
                .flatMap(existingVideo -> {

                    Path destination = UPLOAD_DIR
                        .resolve("course_" + courseId)
                        .resolve("section_" + sectionId)
                        .resolve("lesson_" + lessonId);

                    try {
                        Files.createDirectories(destination);
                    } catch (IOException e) {
                        return Mono.error(new RuntimeException("Не удалось создать директорию для видео", e));
                    }

                    Path oldFilePath = destination.resolve(existingVideo.getFilename());
                    Path newFilePath = destination.resolve(file.filename());

                    return deleteFileIfExists(oldFilePath)        // теперь это Mono<Void>
                        .then(file.transferTo(newFilePath))       // после удаления файла загружаем новый
                        .then(Mono.defer(() -> {                  // затем обновляем запись в БД
                            existingVideo.setFilename(file.filename());
                            existingVideo.setUrl("/api/courses/" + courseId + "/sections/" + sectionId + "/lessons/" + lessonId + "/video/file/" + file.filename());
                            existingVideo.setUploadedAt(LocalDateTime.now());
                            return videoRepository.save(existingVideo);
                        }));
                })
            );
    }

    /**
     * Удалить видео в раздел
     */
    public Mono<Void> deleteVideo(Long courseId, Long sectionId, Long lessonId, Long videoId, Long authorId) {
        return validateAuthor(courseId, sectionId, lessonId, authorId)
            .then(videoRepository.findById(videoId))
            .flatMap(video -> {
                if (video == null) {
                    return Mono.error(new IllegalArgumentException("Video not found with id: " + videoId));
                }

                Path filePath = UPLOAD_DIR
                    .resolve("course_" + courseId)
                    .resolve("section_" + sectionId)
                    .resolve("lesson_" + lessonId)
                    .resolve(video.getFilename());

                return deleteFileIfExists(filePath)
                    .then(videoRepository.deleteById(videoId));
            });
    }

    /**
     * Взять видео в раздел
     */
    public Mono<Video> getVideo(Long courseId, Long sectionId, Long lessonId, Long userId) {
        return validateAuthor(courseId, sectionId, lessonId, userId)
                .flatMap(lesson -> videoRepository.findByLessonId(lesson.getId()));
    }

    private Mono<Void> deleteFileIfExists(Path filePath) {
        return Mono.fromRunnable(() -> {
            try {
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                    log.info("Deleted file: {}", filePath);
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete file: " + filePath, e);
            }
        });
    }

    private Mono<Lesson> validateAuthor(Long courseId, Long sectionId, Long lessonId, Long authorId) {
        return lessonRepository.findById(lessonId)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found")))
            .flatMap(lesson ->
                sectionRepository.findById(sectionId)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found")))
                    .flatMap(section ->
                        courseRepository.findById(courseId)
                            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found")))
                            .flatMap(course -> {
                                if (!course.getAuthorId().equals(authorId)) {
                                    return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Not author of course"));
                                }
                                if (!section.getCourseId().equals(course.getId())) {
                                    return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Section does not belong to course"));
                                }
                                if (!lesson.getSectionId().equals(section.getId())) {
                                    return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lesson does not belong to section"));
                                }
                                return Mono.just(lesson);
                            })
                    )
            );
    }
}
