package com.inhouse.project.repository;

import com.inhouse.project.domain.Casa;
import com.inhouse.project.domain.Proprietario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProprietarioRepository extends JpaRepository<Proprietario, Long> {

    Optional<Proprietario> findByCpf(String cpf);

    Optional<Proprietario> findByUsuarioId(Long usuarioId);

    boolean existsByCpf(String cpf);

    @Query("SELECT COUNT(c) FROM Casa c WHERE c.proprietario.usuario.id = :userId")
    Long countByUserId(@Param("userId") Long userId);

}
