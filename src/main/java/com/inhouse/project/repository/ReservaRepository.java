package com.inhouse.project.repository;

import com.inhouse.project.domain.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

        List<Reserva> findByCasaId(Long casaId);

        List<Reserva> findByHospedePrincipalId(Long hospedeId);

        @Query("SELECT r FROM Reserva r WHERE r.dataCheckin >= :inicio AND r.dataCheckout <= :fim")
        List<Reserva> findReservasNoPeriodo(LocalDate inicio, LocalDate fim);

        @Query("SELECT r FROM Reserva r WHERE r.dataCheckin = :hoje")
        List<Reserva> findReservasParaCheckinHoje(LocalDate hoje);

        @Query("SELECT r FROM Reserva r WHERE r.dataCheckout = :hoje")
        List<Reserva> findReservasParaCheckoutHoje(LocalDate hoje);

        @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Reserva r " +
                        "WHERE r.casa.id = :casaId AND r.status IN ('CONFIRMADA', 'CHECKIN') " +
                        "AND ((:checkin BETWEEN r.dataCheckin AND r.dataCheckout) OR " +
                        "(:checkout BETWEEN r.dataCheckin AND r.dataCheckout) OR " +
                        "(r.dataCheckin >= :checkin AND r.dataCheckout <= :checkout))")
        boolean existeReservaNoPeriodo(@Param("casaId") Long casaId,
                        @Param("checkin") LocalDate checkin,
                        @Param("checkout") LocalDate checkout);

        @Query("SELECT SUM(r.valorTotal) FROM Reserva r WHERE r.status != 'CANCELADA' " +
                        "AND r.dataCheckin >= :inicio AND r.dataCheckout <= :fim")
        BigDecimal calcularFaturamentoPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

        @Query("SELECT r FROM Reserva r WHERE r.status = 'CONFIRMADA' " +
                        "AND r.dataCheckin <= :dataLimite")
        List<Reserva> findReservasVencendoCheckin(@Param("dataLimite") LocalDate dataLimite);

        List<Reserva> findByCasaIdAndStatusIn(Long casaId, List<Reserva.StatusReserva> status);

        @Query(value = """
                        SELECT COALESCE(SUM(r.valor_total), 0)
                        FROM reservas r
                        JOIN casas c ON r.casa_id = c.id
                        JOIN proprietarios p ON c.proprietario_id = p.id
                        JOIN usuarios u ON p.usuario_id = u.id
                        WHERE u.id = :userId
                          AND r.data_checkin >= CURRENT_DATE - interval '30 days'
                        """, nativeQuery = true)
        Double getTotalReservasUltimos30DiasByUserId(@Param("userId") Long userId);

}