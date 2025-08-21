package com.inhouse.project.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "proprietarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Proprietario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    private String endereco;

    @Column(length = 100)
    private String cidade;

    @Column(length = 2)
    private String estado;

    @Column(length = 9)
    private String cep;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    @JsonIgnoreProperties("proprietario")
    private Usuarios usuario;

    @OneToMany(mappedBy = "proprietario", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("proprietario")
    private List<Casa> casas;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
