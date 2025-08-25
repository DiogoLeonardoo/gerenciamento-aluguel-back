package com.inhouse.project.service;

import com.inhouse.project.domain.Hospede;
import com.inhouse.project.exceptions.BusinessException;
import com.inhouse.project.exceptions.ResourceNotFoundException;
import com.inhouse.project.repository.HospedeRepository;
import com.inhouse.project.service.dto.HospedeDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class HospedeService {

    private final HospedeRepository hospedeRepository;
    
    /**
     * Busca um hóspede pelo ID
     * 
     * @param id ID do hóspede a ser buscado
     * @return O hóspede encontrado
     * @throws ResourceNotFoundException se o hóspede não for encontrado
     */
    @Transactional(readOnly = true)
    public Hospede buscarPorId(Long id) {
        return hospedeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Hóspede não encontrado com o ID: " + id));
    }
    
    /**
     * Busca um hóspede pelo CPF
     * 
     * @param cpf CPF do hóspede a ser buscado
     * @return O hóspede encontrado
     * @throws ResourceNotFoundException se o hóspede não for encontrado
     */
    @Transactional(readOnly = true)
    public Hospede buscarPorCpf(String cpf) {
        return hospedeRepository.findByCpf(cpf)
            .orElseThrow(() -> new ResourceNotFoundException("Hóspede não encontrado com o CPF: " + cpf));
    }
    
    /**
     * Lista todos os hóspedes cadastrados
     * 
     * @return Lista de hóspedes
     */
    @Transactional(readOnly = true)
    public List<Hospede> listarTodos() {
        return hospedeRepository.findAll();
    }
    
    /**
     * Salva um novo hóspede
     *
     * @param hospede Dados do hóspede a ser salvo
     * @return O hóspede salvo
     * @throws BusinessException se o CPF já estiver em uso
     */
    @Transactional
    public Hospede salvar(Hospede hospede) {
        // Verificar se o CPF já está em uso
        if (hospede.getId() == null) {
            hospedeRepository.findByCpf(hospede.getCpf()).ifPresent(h -> {
                throw new BusinessException("CPF já cadastrado: " + hospede.getCpf());
            });
        }
        
        return hospedeRepository.save(hospede);
    }
    
    /**
     * Atualiza um hóspede existente
     *
     * @param id ID do hóspede a ser atualizado
     * @param hospedeAtualizado Dados atualizados do hóspede
     * @return O hóspede atualizado
     * @throws ResourceNotFoundException se o hóspede não for encontrado
     * @throws BusinessException se o CPF já estiver em uso por outro hóspede
     */
    @Transactional
    public Hospede atualizar(Long id, Hospede hospedeAtualizado) {
        Hospede hospedeExistente = buscarPorId(id);
        
        // Verificar se o CPF já está em uso por outro hóspede
        if (!hospedeExistente.getCpf().equals(hospedeAtualizado.getCpf())) {
            hospedeRepository.findByCpf(hospedeAtualizado.getCpf()).ifPresent(h -> {
                throw new BusinessException("CPF já cadastrado: " + hospedeAtualizado.getCpf());
            });
        }
        
        // Atualizar campos
        hospedeExistente.setNome(hospedeAtualizado.getNome());
        hospedeExistente.setCpf(hospedeAtualizado.getCpf());
        hospedeExistente.setRg(hospedeAtualizado.getRg());
        hospedeExistente.setTelefone(hospedeAtualizado.getTelefone());
        hospedeExistente.setEmail(hospedeAtualizado.getEmail());
        hospedeExistente.setEndereco(hospedeAtualizado.getEndereco());
        hospedeExistente.setCidade(hospedeAtualizado.getCidade());
        hospedeExistente.setEstado(hospedeAtualizado.getEstado());
        hospedeExistente.setCep(hospedeAtualizado.getCep());
        hospedeExistente.setDataNascimento(hospedeAtualizado.getDataNascimento());
        
        return hospedeRepository.save(hospedeExistente);
    }
    
    /**
     * Exclui um hóspede
     *
     * @param id ID do hóspede a ser excluído
     * @throws ResourceNotFoundException se o hóspede não for encontrado
     */
    @Transactional
    public void excluir(Long id) {
        if (!hospedeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Hóspede não encontrado com o ID: " + id);
        }
        
        hospedeRepository.deleteById(id);
        log.info("Hóspede excluído com ID: {}", id);
    }
}
