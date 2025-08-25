package com.inhouse.project.service.dto;
import com.inhouse.project.domain.Reserva;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaResponse {
    
    private Long id;
    private LocalDate dataCheckin;
    private LocalDate dataCheckout;
    private Integer numPessoas;
    private BigDecimal valorTotal;
    private BigDecimal valorPago;
    private Reserva.StatusReserva status;
    private String observacoes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long diasHospedagem;
    
    private CasaResumo casa;
    private HospedeResumo hospedePrincipal;
    private List<HospedeResumo> hospedes;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CasaResumo {
        private Long id;
        private String nome;
        private String endereco;
        private String cidade;
        private String estado;
        private String cep;
        private Integer quartos;
        private Integer banheiros;
        private Integer maxPessoas;
        private BigDecimal valorDiaria;
        private String descricao;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HospedeResumo {
        private Long id;
        private String nome;
        private String cpf;
        private String email;
        private String telefone;
    }
}