package com.ira.formation.controllers;

import com.ira.formation.dto.VideoDTO;
import com.ira.formation.services.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @PostMapping("/upload/{moduleId}")
    public VideoDTO uploadVideo(
            @PathVariable Long moduleId,
            @RequestParam("file") MultipartFile file) throws Exception {

        return videoService.uploadVideo(moduleId, file);
    }
}