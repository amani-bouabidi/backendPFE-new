package com.ira.formation.controllers;

import com.ira.formation.dto.ApiResponse;
import com.ira.formation.dto.VideoDTO;
import com.ira.formation.services.VideoService;

import lombok.RequiredArgsConstructor;

import java.security.Principal;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    // =================== UPLOAD ===================
    @PostMapping("/upload/{moduleId}")
    public VideoDTO uploadVideo(
            @PathVariable Long moduleId,
            @RequestParam("file") MultipartFile file) throws Exception {

        return videoService.uploadVideo(moduleId, file);
    }

    // =================== DELETE ===================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FORMATEUR')")
    public ApiResponse<Void> deleteVideo(@PathVariable Long id, Principal principal) {

        videoService.deleteVideo(id, principal.getName());

        return ApiResponse.success(null, "Vidéo supprimée avec succès");
    }

    // =================== DOWNLOAD ===================
    @GetMapping("/download/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FORMATEUR','APPRENANT')")
    public ResponseEntity<Resource> downloadVideo(@PathVariable Long id,
                                                   Principal principal) throws Exception {
        return videoService.downloadVideo(id, principal.getName());
    }
}