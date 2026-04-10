package com.ira.formation.controllers;

import com.ira.formation.entities.Video;
import com.ira.formation.repositories.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoStreamingController {

    private final VideoRepository videoRepository;

    private final String uploadDir = "uploads/videos/";

    @GetMapping("/stream/{id}")
    public ResponseEntity<Resource> streamVideo(
            @PathVariable Long id,
            @RequestHeader HttpHeaders headers) throws IOException {

        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vidéo non trouvée"));

        Path path = Paths.get(uploadDir + video.getTitre());

        Resource resource = new UrlResource(path.toUri());

        long fileLength = resource.contentLength();

        HttpRange range = headers.getRange().isEmpty()
                ? null
                : headers.getRange().get(0);

        if (range == null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf("video/mp4"))
                    .contentLength(fileLength)
                    .body(resource);
        }

        long start = range.getRangeStart(fileLength);
        long end = range.getRangeEnd(fileLength);

        long rangeLength = end - start + 1;

        Resource region = new UrlResource(path.toUri());

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaType.valueOf("video/mp4"))
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CONTENT_RANGE,
                        "bytes " + start + "-" + end + "/" + fileLength)
                .contentLength(rangeLength)
                .body(region);
    }
}