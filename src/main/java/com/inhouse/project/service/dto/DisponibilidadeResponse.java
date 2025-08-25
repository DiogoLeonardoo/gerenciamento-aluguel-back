package com.inhouse.project.service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisponibilidadeResponse {
    
    private Long casaId;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private boolean disponivel;
    private String motivo;
    private Long totalDias;
    
    public DisponibilidadeResponse(Long casaId, LocalDate dataInicio, LocalDate dataFim, boolean disponivel) {
        this.casaId = casaId;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.disponivel = disponivel;
        this.totalDias = ChronoUnit.DAYS.between(dataInicio, dataFim);
    }
    
    public DisponibilidadeResponse(Long casaId, LocalDate dataInicio, LocalDate dataFim, boolean disponivel, String motivo) {
        this(casaId, dataInicio, dataFim, disponivel);
        this.motivo = motivo;
    }
}