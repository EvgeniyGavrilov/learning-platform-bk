package com.medical_learning_platform.app.file_loader;

import com.medical_learning_platform.app.product.Product;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@Slf4j
@AllArgsConstructor
@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/video")
public class VideoUploadController {
    private static final Path UPLOAD_DIR = Paths.get("uploads");

    static {
        try {
            Files.createDirectories(UPLOAD_DIR);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create upload directory", e);
        }
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @PostMapping(value = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public Mono<String> uploadVideo(@RequestPart("video") FilePart file) {
        String fileName = file.filename();
        log.info(String.valueOf(file));
        Path destination = UPLOAD_DIR.resolve(fileName);
        return file.transferTo(destination)
                .thenReturn("/api/video/uploaded/" + fileName);
//        return Mono.just("You sent: ");
    }

    @GetMapping("/uploaded/{filename:.+}")
    public Mono<ResponseEntity<Resource>> serveVideo(@PathVariable String filename) {
        Path filePath = UPLOAD_DIR.resolve(filename).normalize();
        FileSystemResource resource = new FileSystemResource(filePath.toFile());

        if (!resource.exists()) {
            return Mono.just(ResponseEntity.notFound().build());
        }

        MediaType mediaType = getMediaType(filename); // см. ниже
        return Mono.just(ResponseEntity.ok()
                .contentType(mediaType)
                .body(resource));
    }

    private MediaType getMediaType(String filename) {
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        return switch (ext) {
            case "mp4" -> MediaType.valueOf("video/mp4");
            case "mov" -> MediaType.valueOf("video/quicktime");
            case "webm" -> MediaType.valueOf("video/webm");
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }
}
