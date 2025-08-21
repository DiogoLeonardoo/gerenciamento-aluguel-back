package com.inhouse.project.repository;

import com.inhouse.project.domain.Usuarios;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuarios, Long> {

    Optional<Usuarios> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Usuarios> findByEmailAndAtivoTrue(String email);
}
