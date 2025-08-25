package com.inhouse.project.service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReservaRequest {
    
    @NotNull(message = "ID da casa é obrigatório")
    private Long casaId;
    
    @NotNull(message = "ID do hóspede principal é obrigatório")
    private Long hospedePrincipalId;
    
    private List<Long> hospedeIds;
    
    @NotNull(message = "Data de check-in é obrigatória")
    @FutureOrPresent(message = "Data de check-in deve ser hoje ou no futuro")
    private LocalDate dataCheckin;
    
    @NotNull(message = "Data de check-out é obrigatória")
    @Future(message = "Data de check-out deve ser no futuro")
    private LocalDate dataCheckout;
    
    @NotNull(message = "Número de pessoas é obrigatório")
    @Min(value = 1, message = "Número de pessoas deve ser pelo menos 1")
    private Integer numPessoas;
    
    @DecimalMin(value = "0.0", message = "Valor pago não pode ser negativo")
    private BigDecimal valorPago;
    
    @Size(max = 1000, message = "Observações não podem exceder 1000 caracteres")
    private String observacoes;
    
    @AssertTrue(message = "Data de check-out deve ser posterior à data de check-in")
    public boolean isDataCheckoutValid() {
        if (dataCheckin == null || dataCheckout == null) {
            return true; 
        }
        return dataCheckout.isAfter(dataCheckin);
    }
}