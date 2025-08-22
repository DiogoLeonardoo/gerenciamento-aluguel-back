package com.inhouse.project.service.dto;

import com.inhouse.project.domain.InventarioCasa;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventarioItemDTO {
    
    @NotBlank(message = "Nome do item é obrigatório")
    private String item;
    
    private String descricao;
    
    @Min(value = 1, message = "Quantidade deve ser pelo menos 1")
    private Integer quantidade = 1;
    
    @NotNull(message = "A condição do item é obrigatória")
    private InventarioCasa.CondicaoItem condicao;
    
    private BigDecimal valorEstimado;
    
    private String observacoes;
    
    /**
     * Converte o DTO para a entidade InventarioCasa
     * 
     * @return Objeto InventarioCasa
     */
    public InventarioCasa toEntity() {
        InventarioCasa inventarioCasa = new InventarioCasa();
        inventarioCasa.setItem(this.item);
        inventarioCasa.setDescricao(this.descricao);
        inventarioCasa.setQuantidade(this.quantidade);
        inventarioCasa.setCondicao(this.condicao);
        inventarioCasa.setValorEstimado(this.valorEstimado);
        inventarioCasa.setObservacoes(this.observacoes);
        return inventarioCasa;
    }
}
