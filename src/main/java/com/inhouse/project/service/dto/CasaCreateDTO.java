package com.inhouse.project.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Data
@ToString(exclude = "fotos") 
public class CasaCreateDTO {
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
    
    private List<MultipartFile> fotos;
    
    public void setQuartos(String quartos) {
        if (quartos != null && !quartos.isEmpty()) {
            this.quartos = Integer.parseInt(quartos);
        }
    }
    
    public void setBanheiros(String banheiros) {
        if (banheiros != null && !banheiros.isEmpty()) {
            this.banheiros = Integer.parseInt(banheiros);
        }
    }
    
    public void setMaxPessoas(String maxPessoas) {
        if (maxPessoas != null && !maxPessoas.isEmpty()) {
            this.maxPessoas = Integer.parseInt(maxPessoas);
        }
    }
    
    public void setValorDiaria(String valorDiaria) {
        if (valorDiaria != null && !valorDiaria.isEmpty()) {
            this.valorDiaria = new BigDecimal(valorDiaria);
        }
    }
    
    // Métodos adicionais para aceitar Integer diretamente (para requisições JSON)
    public void setQuartos(Integer quartos) {
        this.quartos = quartos;
    }
    
    public void setBanheiros(Integer banheiros) {
        this.banheiros = banheiros;
    }
    
    public void setMaxPessoas(Integer maxPessoas) {
        this.maxPessoas = maxPessoas;
    }
    
    public void setValorDiaria(BigDecimal valorDiaria) {
        this.valorDiaria = valorDiaria;
    }
}
