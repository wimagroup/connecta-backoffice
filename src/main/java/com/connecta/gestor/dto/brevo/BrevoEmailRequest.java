package com.connecta.gestor.dto.brevo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrevoEmailRequest {
    
    @JsonProperty("sender")
    private BrevoContact sender;
    
    @JsonProperty("to")
    private List<BrevoContact> to;
    
    @JsonProperty("replyTo")
    private BrevoContact replyTo;
    
    @JsonProperty("subject")
    private String subject;
    
    @JsonProperty("htmlContent")
    private String htmlContent;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BrevoContact {
        @JsonProperty("email")
        private String email;
        
        @JsonProperty("name")
        private String name;
    }
}

