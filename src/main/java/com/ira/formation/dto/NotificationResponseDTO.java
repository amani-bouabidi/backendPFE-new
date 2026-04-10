package com.ira.formation.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponseDTO {

    private Long id;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;
}