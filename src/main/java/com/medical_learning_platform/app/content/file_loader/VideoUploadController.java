package com.medical_learning_platform.app.content.file_loader;

import com.medical_learning_platform.app.content.videos.VideoService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/video")
public class VideoUploadController {
//    private static final Path UPLOAD_DIR = Paths.get("uploads");
    private final VideoService videoService;

    static {
        try {
//            Files.createDirectories(UPLOAD_DIR);
            Files.createDirectories(UploadFileUtils.UPLOAD_DIR);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create upload directory", e);
        }
    }

    @PostMapping(value = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public Mono<String> uploadVideo(@RequestPart("video") FilePart file) {
        String fileName = file.filename();
        log.info(String.valueOf(file));
//        Path destination = UPLOAD_DIR.resolve(fileName);
        Path destination = UploadFileUtils.UPLOAD_DIR.resolve(fileName);
        return file.transferTo(destination)
                .thenReturn("/api/video/uploaded/" + fileName);
//        return Mono.just("You sent: ");
    }

    @GetMapping("/uploaded/{filename:.+}")
    public Mono<ResponseEntity<Resource>> serveVideo(@PathVariable String filename) {
//        Path filePath = UPLOAD_DIR.resolve(filename).normalize();
        Path filePath = UploadFileUtils.UPLOAD_DIR.resolve(filename).normalize();
        FileSystemResource resource = new FileSystemResource(filePath.toFile());

        if (!resource.exists()) {
            return Mono.just(ResponseEntity.notFound().build());
        }

        MediaType mediaType = videoService.getMediaType(filename); // см. ниже
        return Mono.just(ResponseEntity.ok()
            .contentType(mediaType)
            .body(resource));
    }

    @GetMapping("/uploaded/{courseId}/{sectionId}/{lessonId}/{filename:.+}")
    public Mono<ResponseEntity<Resource>> serveVideo(
        @PathVariable Long courseId,
        @PathVariable Long sectionId,
        @PathVariable Long lessonId,
        @PathVariable String filename,
        Authentication authentication
    ){
        Long userId = Long.parseLong((String) authentication.getPrincipal());
        return videoService.serveVideo(courseId, sectionId, lessonId, filename, userId);
    }
}
