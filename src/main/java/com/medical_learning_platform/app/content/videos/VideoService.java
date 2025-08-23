package com.medical_learning_platform.app.content.videos;


import com.medical_learning_platform.app.content.courses.repository.CourseAccessRepository;
import com.medical_learning_platform.app.content.courses.repository.CourseRepository;
import com.medical_learning_platform.app.content.file_loader.VideoFileUtils;
import com.medical_learning_platform.app.content.lesson.entity.Lesson;
import com.medical_learning_platform.app.content.lesson.repository.LessonRepository;
import com.medical_learning_platform.app.content.sections.repository.SectionRepository;
import com.medical_learning_platform.app.content.videos.entity.Video;
import com.medical_learning_platform.app.content.videos.repository.VideoRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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

//    private static final Path UPLOAD_DIR = Paths.get("uploads");

    /**
     * Загрузить видео в раздел
     */
    public Mono<Video> addVideo(Long courseId, Long sectionId, Long lessonId, FilePart file, Long authorId) {
        return validateAuthor(courseId, sectionId, lessonId, authorId)
            .flatMap(lesson -> {
                String filename = file.filename();
                Path lessonDir = VideoFileUtils.getLessonDir(courseId, sectionId, lessonId);

                return Mono.fromRunnable(() -> {
                    try {
                        Files.createDirectories(lessonDir);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then(videoRepository.findByLessonId(lessonId)
                    .flatMap(existingVideo -> {
                        Path oldFile = lessonDir.resolve(existingVideo.getFilename());
                        return VideoFileUtils.deleteFile(oldFile)
                            .then(videoRepository.delete(existingVideo));
                    })
                    .then(file.transferTo(lessonDir.resolve(filename)))
                    .then(Mono.defer(() -> {
                        Video video = new Video();
                        video.setLessonId(lessonId);
                        video.setFilename(filename);
                        video.setUrl("/api/video/uploaded/" + courseId + "/" + sectionId + "/" + lessonId + "/" + filename);
                        video.setUploadedAt(LocalDateTime.now());
                        return videoRepository.save(video);
                    }))
                );
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

                    Path lessonDir = VideoFileUtils.getLessonDir(courseId, sectionId, lessonId);
                    Path oldFile = lessonDir.resolve(existingVideo.getFilename());
                    Path newFile = lessonDir.resolve(file.filename());

                    return Mono.fromRunnable(() -> {
                        try {
                            Files.createDirectories(lessonDir);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .subscribeOn(Schedulers.boundedElastic())
                    .then(VideoFileUtils.deleteFile(oldFile))
                    .then(file.transferTo(newFile))
                    .then(Mono.defer(() -> {
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
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Video not found")))
            .flatMap(video -> {
                Path filePath = VideoFileUtils.getLessonDir(courseId, sectionId, lessonId).resolve(video.getFilename());
                return VideoFileUtils.deleteFile(filePath)
                    .then(videoRepository.deleteById(videoId))
                    .then(VideoFileUtils.checkAndDeleteEmptyLessonDir(courseId, sectionId, lessonId));
            });
    }

    /**
     * Взять видео в раздел
     */
    public Mono<Video> getVideo(Long courseId, Long sectionId, Long lessonId, Long userId) {
        return validateAuthor(courseId, sectionId, lessonId, userId)
                .flatMap(lesson -> videoRepository.findByLessonId(lesson.getId()));
    }

    public Mono<ResponseEntity<Resource>> serveVideo(Long courseId, Long sectionId, Long lessonId, String filename) {
        Path filePath = VideoFileUtils.UPLOAD_DIR
            .resolve("course_" + courseId)
            .resolve("section_" + sectionId)
            .resolve("lesson_" + lessonId)
            .resolve(filename)
            .normalize();

        FileSystemResource resource = new FileSystemResource(filePath.toFile());

        if (!resource.exists()) {
            return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Video not found"));
        }

        MediaType mediaType = getMediaType(filename);

        return Mono.just(
            ResponseEntity.ok()
                .contentType(mediaType)
                .body(resource)
        );
    }

    public MediaType getMediaType(String filename) {
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        return switch (ext) {
            case "mp4" -> MediaType.valueOf("video/mp4");
            case "webm" -> MediaType.valueOf("video/webm");
            case "ogg" -> MediaType.valueOf("video/ogg");
            default -> MediaType.APPLICATION_OCTET_STREAM; // fallback
        };
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
