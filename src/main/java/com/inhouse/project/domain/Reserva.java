package com.inhouse.project.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "reservas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "casa_id")
    private Casa casa;

    @ManyToOne
    @JoinColumn(name = "hospede_principal_id")
    private Hospede hospedePrincipal;

    @Column(name = "data_checkin", nullable = false)
    private LocalDate dataCheckin;

    @Column(name = "data_checkout", nullable = false)
    private LocalDate dataCheckout;

    @Column(name = "num_pessoas", nullable = false)
    private Integer numPessoas;

    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "valor_pago", precision = 10, scale = 2)
    private BigDecimal valorPago = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private StatusReserva status = StatusReserva.PENDENTE;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @ManyToMany
    @JoinTable(
            name = "reserva_hospedes",
            joinColumns = @JoinColumn(name = "reserva_id"),
            inverseJoinColumns = @JoinColumn(name = "hospede_id")
    )
    private List<Hospede> hospedes;

    @OneToOne(mappedBy = "reserva", cascade = CascadeType.ALL)
    private Contrato contrato;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum StatusReserva {
        PENDENTE, CONFIRMADA, CHECKIN, CHECKOUT, CANCELADA
    }
}
