package com.medical_learning_platform.app.content.file_loader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

@Slf4j
@Service
public class VideoFileUtils {

    public static final Path UPLOAD_DIR = Paths.get("uploads");

    public static Path getLessonDir(Long courseId, Long sectionId, Long lessonId) {
        return UPLOAD_DIR
            .resolve("course_" + courseId)
            .resolve("section_" + sectionId)
            .resolve("lesson_" + lessonId);
    }

    public static Mono<Void> deleteFile(Path filePath) {
        return Mono.fromRunnable(() -> {
            try {
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                    log.info("Deleted file: {}", filePath);
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete file: " + filePath, e);
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    public static Mono<Void> deleteDirectoryRecursively(Path path) {
        return Mono.fromRunnable(() -> {
            if (Files.exists(path)) {
                try {
                    Files.walk(path)
                        .sorted(Comparator.reverseOrder())
                        .forEach(p -> {
                            try {
                                Files.delete(p);
                                log.info("Deleted: {}", p);
                            } catch (IOException e) {
                                throw new RuntimeException("Failed to delete: " + p, e);
                            }
                        });
                } catch (IOException e) {
                    throw new RuntimeException("Failed to walk directory: " + path, e);
                }
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    public static Mono<Void> checkAndDeleteEmptyLessonDir(Long courseId, Long sectionId, Long lessonId) {
        Path lessonDir = getLessonDir(courseId, sectionId, lessonId);
        return Mono.fromRunnable(() -> {
            if (Files.exists(lessonDir)) {
                try (Stream<Path> files = Files.list(lessonDir)) {
                    if (files.findAny().isEmpty()) {
                        Files.delete(lessonDir);
                        log.info("Deleted empty lesson directory: {}", lessonDir);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Failed to check/delete lesson directory: " + lessonDir, e);
                }
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
