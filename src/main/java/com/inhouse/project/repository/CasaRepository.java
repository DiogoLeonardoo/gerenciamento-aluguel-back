package com.inhouse.project.repository;

import com.inhouse.project.domain.Casa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CasaRepository extends JpaRepository<Casa, Long> {

    List<Casa> findByAtivaTrue();

    List<Casa> findByProprietarioId(Long proprietarioId);

    @Query("SELECT c FROM Casa c WHERE c.ativa = true AND c.cidade ILIKE %:cidade%")
    List<Casa> findByCidadeIgnoreCase(@Param("cidade") String cidade);

    @Query("SELECT c FROM Casa c WHERE c.ativa = true AND c.maxPessoas >= :pessoas")
    List<Casa> findByCapacidadeMinima(@Param("pessoas") Integer pessoas);

    @Query("SELECT c FROM Casa c WHERE c.ativa = true AND c.valorDiaria BETWEEN :minValor AND :maxValor")
    List<Casa> findByFaixaPreco(@Param("minValor") BigDecimal minValor, @Param("maxValor") BigDecimal maxValor);

    @Query("SELECT c FROM Casa c WHERE c.ativa = true AND c.id NOT IN " +
            "(SELECT r.casa.id FROM Reserva r WHERE r.status IN ('CONFIRMADA', 'CHECKIN') AND " +
            "((:checkin BETWEEN r.dataCheckin AND r.dataCheckout) OR " +
            "(:checkout BETWEEN r.dataCheckin AND r.dataCheckout) OR " +
            "(r.dataCheckin >= :checkin AND r.dataCheckout <= :checkout)))")
    List<Casa> findCasasDisponiveis(@Param("checkin") LocalDate checkin,
                                    @Param("checkout") LocalDate checkout);

    @Query("SELECT c FROM Casa c WHERE c.ativa = true " +
            "AND (:cidade IS NULL OR c.cidade ILIKE %:cidade%) " +
            "AND (:pessoas IS NULL OR c.maxPessoas >= :pessoas) " +
            "AND (:minValor IS NULL OR c.valorDiaria >= :minValor) " +
            "AND (:maxValor IS NULL OR c.valorDiaria <= :maxValor) " +
            "AND c.id NOT IN (SELECT r.casa.id FROM Reserva r WHERE r.status IN ('CONFIRMADA', 'CHECKIN') AND " +
            "((:checkin BETWEEN r.dataCheckin AND r.dataCheckout) OR " +
            "(:checkout BETWEEN r.dataCheckin AND r.dataCheckout) OR " +
            "(r.dataCheckin >= :checkin AND r.dataCheckout <= :checkout)))")
    List<Casa> findCasasComFiltros(@Param("cidade") String cidade,
                                   @Param("pessoas") Integer pessoas,
                                   @Param("minValor") BigDecimal minValor,
                                   @Param("maxValor") BigDecimal maxValor,
                                   @Param("checkin") LocalDate checkin,
                                   @Param("checkout") LocalDate checkout);

    List<Casa> findByProprietarioUsuarioId(Long usuarioId);
}