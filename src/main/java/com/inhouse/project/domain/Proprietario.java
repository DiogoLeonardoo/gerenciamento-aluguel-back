package com.inhouse.project.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "proprietarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Proprietario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
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

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "proprietario_hospedes", joinColumns = @JoinColumn(name = "proprietario_id"), inverseJoinColumns = @JoinColumn(name = "hospede_id"))
    private Set<Hospede> hospedes = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
