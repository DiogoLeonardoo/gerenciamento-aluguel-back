package com.inhouse.project.repository;

import com.inhouse.project.domain.TemplateContrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TemplateContratoRepository extends JpaRepository<TemplateContrato, Long> {

    List<TemplateContrato> findByAtivoTrueOrderByCreatedAtDesc();

    Optional<TemplateContrato> findByNomeAndAtivoTrue(String nome);

    List<TemplateContrato> findByAtivoTrue();
}
