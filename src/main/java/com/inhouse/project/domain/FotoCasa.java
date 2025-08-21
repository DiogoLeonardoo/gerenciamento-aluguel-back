package com.inhouse.project.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fotos_casas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class FotoCasa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "casa_id")
    @JsonIgnore
    private Casa casa;

    @Lob
    @Column(name = "conteudo_arquivo", nullable = false)
    @JsonIgnore
    private byte[] conteudoArquivo;

    @Column(length = 100)
    private String nomeArquivo;

    @Column(length = 100)
    private String descricao;
    
    @Column(length = 50)
    private String contentType;
    
    @Column(name = "tamanho")
    private Long tamanho;

    @Column(columnDefinition = "boolean default false")
    private Boolean principal = false;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
