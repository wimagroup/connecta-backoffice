package com.connecta.gestor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponseDTO {
    
    private String message;
    private LocalDateTime timestamp;
    
    public static MessageResponseDTO of(String message) {
        return MessageResponseDTO.builder()
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}

