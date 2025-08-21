package com.inhouse.project.repository;

import com.inhouse.project.domain.Proprietario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProprietarioRepository extends JpaRepository<Proprietario, Long> {

    Optional<Proprietario> findByCpf(String cpf);

    Optional<Proprietario> findByUsuarioId(Long usuarioId);

    boolean existsByCpf(String cpf);
}
