package com.inhouse.project.repository;

import com.inhouse.project.domain.Casa;
import com.inhouse.project.domain.InventarioCasa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventarioCasaRepository extends JpaRepository<InventarioCasa, Long> {
    
    /**
     * Busca todos os itens do inventário de uma casa específica
     * 
     * @param casa A casa cujos itens serão buscados
     * @return Lista de itens do inventário
     */
    List<InventarioCasa> findByCasa(Casa casa);
    
    /**
     * Busca todos os itens do inventário de uma casa pelo ID da casa
     * 
     * @param casaId ID da casa
     * @return Lista de itens do inventário
     */
    List<InventarioCasa> findByCasaId(Long casaId);
    
    /**
     * Verifica se um item existe para uma casa
     * 
     * @param casaId ID da casa
     * @param item Nome do item
     * @return true se o item existe, false caso contrário
     */
    boolean existsByCasaIdAndItemIgnoreCase(Long casaId, String item);
}
