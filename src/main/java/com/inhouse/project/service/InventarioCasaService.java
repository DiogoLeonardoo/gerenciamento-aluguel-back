package com.inhouse.project.service;

import com.inhouse.project.domain.Casa;
import com.inhouse.project.domain.InventarioCasa;
import com.inhouse.project.domain.Usuarios;
import com.inhouse.project.exceptions.BusinessException;
import com.inhouse.project.exceptions.ResourceNotFoundException;
import com.inhouse.project.repository.CasaRepository;
import com.inhouse.project.repository.InventarioCasaRepository;
import com.inhouse.project.service.dto.InventarioItemDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InventarioCasaService {

    private final InventarioCasaRepository inventarioCasaRepository;
    private final CasaRepository casaRepository;
    private final UsuarioService usuarioService;
    
    /**
     * Adiciona um item ao inventário de uma casa
     * 
     * @param casaId ID da casa
     * @param itemDTO Dados do item a ser adicionado
     * @return O item adicionado
     */
    public InventarioCasa adicionarItem(Long casaId, InventarioItemDTO itemDTO) {
        // Verificar permissões - apenas proprietário da casa pode adicionar itens
        Usuarios usuarioLogado = usuarioService.getUsuarioLogado();
        
        Casa casa = casaRepository.findById(casaId)
                .orElseThrow(() -> new ResourceNotFoundException("Casa não encontrada com ID: " + casaId));
                
        // Verifica se o usuário logado é o proprietário da casa ou um administrador
        if (usuarioLogado.getRole() != Usuarios.Role.ADMIN && 
            (casa.getProprietario() == null || 
             casa.getProprietario().getUsuario() == null ||
             !casa.getProprietario().getUsuario().getId().equals(usuarioLogado.getId()))) {
            throw new BusinessException("Você não tem permissão para adicionar itens ao inventário desta casa");
        }
        
        // Verificar se o item já existe (evitar duplicidade)
        if (inventarioCasaRepository.existsByCasaIdAndItemIgnoreCase(casaId, itemDTO.getItem())) {
            throw new BusinessException("Já existe um item com este nome no inventário desta casa");
        }
        
        // Criar e salvar o novo item
        InventarioCasa novoItem = itemDTO.toEntity();
        novoItem.setCasa(casa);
        
        InventarioCasa itemSalvo = inventarioCasaRepository.save(novoItem);
        log.info("Item adicionado ao inventário da casa ID {}: {}", casaId, itemSalvo.getItem());
        
        return itemSalvo;
    }
    
    /**
     * Lista todos os itens do inventário de uma casa
     * 
     * @param casaId ID da casa
     * @return Lista de itens do inventário
     */
    public List<InventarioCasa> listarItensPorCasa(Long casaId) {
        if (!casaRepository.existsById(casaId)) {
            throw new ResourceNotFoundException("Casa não encontrada com ID: " + casaId);
        }
        
        return inventarioCasaRepository.findByCasaId(casaId);
    }
    
    /**
     * Busca um item específico do inventário
     * 
     * @param id ID do item
     * @return O item encontrado
     */
    public InventarioCasa buscarItemPorId(Long id) {
        return inventarioCasaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item de inventário não encontrado com ID: " + id));
    }
    
    /**
     * Atualiza um item do inventário
     * 
     * @param id ID do item
     * @param itemDTO Novos dados do item
     * @return O item atualizado
     */
    public InventarioCasa atualizarItem(Long id, InventarioItemDTO itemDTO) {
        InventarioCasa itemExistente = buscarItemPorId(id);
        
        // Verificar permissões
        Usuarios usuarioLogado = usuarioService.getUsuarioLogado();
        Casa casa = itemExistente.getCasa();
        
        // Verifica se o usuário logado é o proprietário da casa ou um administrador
        if (usuarioLogado.getRole() != Usuarios.Role.ADMIN && 
            (casa.getProprietario() == null || 
             casa.getProprietario().getUsuario() == null ||
             !casa.getProprietario().getUsuario().getId().equals(usuarioLogado.getId()))) {
            throw new BusinessException("Você não tem permissão para atualizar itens do inventário desta casa");
        }
        
        // Verifica se o novo nome já existe em outro item (exceto o atual)
        if (!itemExistente.getItem().equalsIgnoreCase(itemDTO.getItem()) && 
            inventarioCasaRepository.existsByCasaIdAndItemIgnoreCase(casa.getId(), itemDTO.getItem())) {
            throw new BusinessException("Já existe outro item com este nome no inventário desta casa");
        }
        
        // Atualizar os dados
        itemExistente.setItem(itemDTO.getItem());
        itemExistente.setDescricao(itemDTO.getDescricao());
        itemExistente.setQuantidade(itemDTO.getQuantidade());
        itemExistente.setCondicao(itemDTO.getCondicao());
        itemExistente.setValorEstimado(itemDTO.getValorEstimado());
        itemExistente.setObservacoes(itemDTO.getObservacoes());
        
        return inventarioCasaRepository.save(itemExistente);
    }
    
    /**
     * Remove um item do inventário
     * 
     * @param id ID do item
     */
    public void removerItem(Long id) {
        InventarioCasa item = buscarItemPorId(id);
        
        // Verificar permissões
        Usuarios usuarioLogado = usuarioService.getUsuarioLogado();
        Casa casa = item.getCasa();
        
        // Verifica se o usuário logado é o proprietário da casa ou um administrador
        if (usuarioLogado.getRole() != Usuarios.Role.ADMIN && 
            (casa.getProprietario() == null || 
             casa.getProprietario().getUsuario() == null ||
             !casa.getProprietario().getUsuario().getId().equals(usuarioLogado.getId()))) {
            throw new BusinessException("Você não tem permissão para remover itens do inventário desta casa");
        }
        
        inventarioCasaRepository.delete(item);
        log.info("Item removido do inventário: {} (Casa ID: {})", item.getItem(), casa.getId());
    }
}
