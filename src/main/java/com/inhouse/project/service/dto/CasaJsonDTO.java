package com.inhouse.project.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO para receber dados de casa em formato JSON
 */
@Data
public class CasaJsonDTO {
    private String nome;
    private String descricao;
    private String endereco;
    private String cidade;
    
    // Deve ser a sigla do estado (2 caracteres)
    private String estado;
    
    private String cep;
    
    @JsonProperty("quartos")
    private Integer quartos;
    
    @JsonProperty("banheiros")
    private Integer banheiros;
    
    @JsonProperty("maxPessoas")
    private Integer maxPessoas;
    
    @JsonProperty("valorDiaria")
    private BigDecimal valorDiaria;
    
    /**
     * Converte este DTO para CasaCreateDTO (sem fotos)
     */
    public CasaCreateDTO toCasaCreateDTO() {
        CasaCreateDTO dto = new CasaCreateDTO();
        dto.setNome(this.nome);
        dto.setDescricao(this.descricao);
        dto.setEndereco(this.endereco);
        dto.setCidade(this.cidade);
        dto.setEstado(this.estado);
        dto.setCep(this.cep);
        dto.setQuartos(this.quartos);
        dto.setBanheiros(this.banheiros);
        dto.setMaxPessoas(this.maxPessoas);
        dto.setValorDiaria(this.valorDiaria);
        return dto;
    }
}
