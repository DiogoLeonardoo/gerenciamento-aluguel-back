package com.inhouse.project.repository;

import com.inhouse.project.domain.Contrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    Optional<Contrato> findByReservaId(Long reservaId);

    List<Contrato> findByAssinadoTrue();

    List<Contrato> findByAssinadoFalse();

    @Query("SELECT c FROM Contrato c JOIN c.reserva r WHERE r.casa.proprietario.id = :proprietarioId")
    List<Contrato> findByProprietarioId(@Param("proprietarioId") Long proprietarioId);

    boolean existsByReservaId(Long reservaId);
}

