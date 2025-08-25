package com.inhouse.project.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelamentoRequest {
    
    @NotBlank(message = "Motivo do cancelamento é obrigatório")
    @Size(min = 5, max = 500, message = "Motivo deve ter entre 5 e 500 caracteres")
    private String motivo;
}