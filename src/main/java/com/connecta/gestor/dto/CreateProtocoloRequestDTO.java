package com.connecta.gestor.dto;

import com.connecta.gestor.model.enums.PrioridadeProtocolo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateProtocoloRequestDTO {
    
    @NotNull(message = "Serviço é obrigatório")
    private Long servicoId;
    
    @NotBlank(message = "Nome do cidadão é obrigatório")
    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
    private String cidadaoNome;
    
    @Email(message = "Email inválido")
    @Size(max = 150, message = "Email deve ter no máximo 150 caracteres")
    private String cidadaoEmail;
    
    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    private String cidadaoTelefone;
    
    @NotBlank(message = "Descrição do problema é obrigatória")
    private String descricaoProblema;
    
    private PrioridadeProtocolo prioridade;
    
    @NotNull(message = "Dados do protocolo são obrigatórios")
    private List<ProtocoloDadoRequestDTO> dados;
}

