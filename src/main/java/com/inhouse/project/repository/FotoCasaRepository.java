package com.inhouse.project.repository;

import com.inhouse.project.domain.FotoCasa;
import com.inhouse.project.domain.Casa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FotoCasaRepository extends JpaRepository<FotoCasa, Long> {

    // Lista todas as fotos de uma casa
    List<FotoCasa> findByCasa(Casa casa);

    // Pega a foto principal de uma casa
    FotoCasa findByCasaAndPrincipalTrue(Casa casa);
    
    // Busca a primeira foto da casa (preferencialmente a principal)
    java.util.Optional<FotoCasa> findFirstByCasaIdOrderByPrincipalDesc(Long casaId);
    
    // Lista todas as fotos de uma casa pelo ID da casa
    List<FotoCasa> findByCasaId(Long casaId);
}
