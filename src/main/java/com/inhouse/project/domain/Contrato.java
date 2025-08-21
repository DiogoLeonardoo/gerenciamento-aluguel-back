package com.inhouse.project.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contratos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "reserva_id")
    private Reserva reserva;

    @ManyToOne
    @JoinColumn(name = "template_id")
    private TemplateContrato template;

    @Column(name = "conteudo_contrato", nullable = false, columnDefinition = "TEXT")
    private String conteudoContrato;

    @Column(name = "data_geracao")
    @CreationTimestamp
    private LocalDateTime dataGeracao;

    @Column(columnDefinition = "boolean default false")
    private Boolean assinado = false;

    @Column(name = "data_assinatura")
    private LocalDateTime dataAssinatura;

    @Column(name = "caminho_arquivo")
    private String caminhoArquivo;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}