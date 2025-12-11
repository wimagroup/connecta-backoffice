package com.connecta.gestor.dto.brevo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrevoEmailResponse {
    
    @JsonProperty("messageId")
    private String messageId;
    
    @JsonProperty("code")
    private String code;
    
    @JsonProperty("message")
    private String message;
}

