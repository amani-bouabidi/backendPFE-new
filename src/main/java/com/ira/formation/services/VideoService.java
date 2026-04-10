package com.ira.formation.services;

import com.ira.formation.dto.VideoDTO;
import com.ira.formation.entities.Module;
import com.ira.formation.entities.Video;
import com.ira.formation.repositories.ModuleRepository;
import com.ira.formation.repositories.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;
    private final ModuleRepository moduleRepository;

    private final String uploadDir = System.getProperty("user.dir") + "/uploads/videos/";

    @PreAuthorize("hasRole('ADMIN')")
    public VideoDTO uploadVideo(Long moduleId, MultipartFile file) throws IOException {

        String contentType = file.getContentType();

        if (contentType == null || !contentType.equals("video/mp4")) {
            throw new RuntimeException("Seuls les fichiers MP4 sont autorisés");
        }

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module non trouvé"));

        // ✅ create folder
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // ✅ avoid duplicate names
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        File destination = new File(uploadDir + fileName);

        file.transferTo(destination);

        Video video = Video.builder()
                .titre(fileName)
                .filePath("/videos/" + fileName) // ✅ مهم
                .module(module)
                .build();

        Video saved = videoRepository.save(video);

        return VideoDTO.builder()
                .id(saved.getId())
                .titre(saved.getTitre())
                .filePath(saved.getFilePath())
                .moduleId(moduleId)
                .build();
    }
}