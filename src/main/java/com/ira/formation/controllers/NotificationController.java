package com.ira.formation.controllers;

import com.ira.formation.dto.ApiResponse;
import com.ira.formation.dto.NotificationResponseDTO;
import com.ira.formation.entities.Notification;
import com.ira.formation.services.NotificationService;
import lombok.RequiredArgsConstructor;

import java.security.Principal;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('APPRENANT')")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<List<NotificationResponseDTO>> getNotifications(Principal principal){

        List<NotificationResponseDTO> list =
                notificationService.getUserNotifications(principal.getName());

        return ApiResponse.success(list, "Notifications récupérées");
    }
    
    @PutMapping("/{id}/read")
    public ApiResponse<Object> markAsRead(@PathVariable Long id,
                                          Principal principal){

        notificationService.markAsRead(id, principal.getName());

        return ApiResponse.success(null, "Notification marquée comme lue");
    }
}