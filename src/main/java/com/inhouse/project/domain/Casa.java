package com.inhouse.project.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "casas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Casa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "proprietario_id")
    @JsonIgnoreProperties("casas")
    private Proprietario proprietario;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String endereco;

    @Column(nullable = false, length = 100)
    private String cidade;

    @Column(nullable = false, length = 2)
    private String estado;

    @Column(nullable = false, length = 9)
    private String cep;

    @Column(nullable = false)
    private Integer quartos;

    @Column(nullable = false)
    private Integer banheiros;

    @Column(name = "max_pessoas", nullable = false)
    private Integer maxPessoas;

    @Column(name = "valor_diaria", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorDiaria;

    @Column(columnDefinition = "boolean default true")
    private Boolean ativa = true;

    @OneToMany(mappedBy = "casa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InventarioCasa> inventario;

    @OneToMany(mappedBy = "casa", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<FotoCasa> fotos;
    
    // Método auxiliar para obter informações sobre as fotos
    @com.fasterxml.jackson.annotation.JsonProperty("fotosInfo")
    public java.util.Map<String, String> getFotosInfo() {
        if (id == null) {
            return null;
        }
        
        java.util.Map<String, String> info = new java.util.HashMap<>();
        info.put("listaFotos", "/api/fotos/lista/casa/" + id);
        info.put("fotoPrincipal", "/api/fotos/casa/" + id + "/principal");
        info.put("uploadFoto", "/api/fotos/upload/casa/" + id);
        info.put("uploadMultiplasFotos", "/api/fotos/upload/multiplas/casa/" + id);
        
        return info;
    }
    
    // Método auxiliar para obter as URLs das fotos individuais
    @com.fasterxml.jackson.annotation.JsonProperty("fotosUrls")
    public List<String> getFotosUrls() {
        if (id == null) {
            return java.util.Collections.emptyList();
        }
        
        // Não acessamos a lista de fotos diretamente para evitar problemas com o LOB
        return java.util.Collections.singletonList("/api/fotos/casa/" + id + "/principal");
    }

    @OneToMany(mappedBy = "casa")
    private List<Reserva> reservas;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
