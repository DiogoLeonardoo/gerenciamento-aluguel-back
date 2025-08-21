package com.inhouse.project.service.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CasaUpdateDTO {
    private String nome;
    private String descricao;
    private String endereco;
    private String cidade;
    private String estado;
    private String cep;
    private Integer quartos;
    private Integer banheiros;
    private Integer maxPessoas;
    private BigDecimal valorDiaria;

    // Novas fotos para adicionar
    private List<MultipartFile> fotosNovas;

    // IDs de fotos existentes que devem ser removidas
    private List<Long> fotosExcluir;

}
