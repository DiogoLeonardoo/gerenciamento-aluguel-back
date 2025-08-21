package com.inhouse.project.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventario_casas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventarioCasa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "casa_id")
    private Casa casa;

    @Column(nullable = false, length = 100)
    private String item;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(columnDefinition = "integer default 1")
    private Integer quantidade = 1;

    @Enumerated(EnumType.STRING)
    private CondicaoItem condicao;

    @Column(name = "valor_estimado", precision = 10, scale = 2)
    private BigDecimal valorEstimado;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum CondicaoItem {
        NOVA, BOA, REGULAR, RUIM
    }
}