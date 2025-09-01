package com.inhouse.project.repository;

import com.inhouse.project.domain.Hospede;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HospedeRepository extends JpaRepository<Hospede, Long> {
    Optional<Hospede> findByCpf(String cpf);

     @Query(value = """
        SELECT COUNT(ph.hospede_id)
        FROM proprietario_hospedes ph
        WHERE ph.proprietario_id = :proprietarioId
        """, nativeQuery = true)
    Long countHospedesByProprietarioId(@Param("proprietarioId") Long proprietarioId);
}