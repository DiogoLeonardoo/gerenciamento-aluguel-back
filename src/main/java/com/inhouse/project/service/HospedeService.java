package com.inhouse.project.service;

import com.inhouse.project.domain.Hospede;
import com.inhouse.project.domain.Proprietario;
import com.inhouse.project.exceptions.BusinessException;
import com.inhouse.project.exceptions.ResourceNotFoundException;
import com.inhouse.project.repository.HospedeRepository;
import com.inhouse.project.repository.ProprietarioRepository;
import com.inhouse.project.service.dto.HospedeDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HospedeService {

    private final HospedeRepository hospedeRepository;
    private final ProprietarioRepository proprietarioRepository;

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
    public Hospede salvar(Hospede hospede, Long proprietarioId) {
        Optional<Hospede> hospedeExistenteOpt = hospedeRepository.findByCpf(hospede.getCpf());

        Proprietario proprietario = proprietarioRepository.findById(proprietarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Proprietário não encontrado"));

        Hospede hospedeSalvo;

        if (hospedeExistenteOpt.isPresent()) {
            hospedeSalvo = hospedeExistenteOpt.get();
        } else {
            hospedeSalvo = hospedeRepository.save(hospede);
        }

        // Atualiza os dois lados da relação
        hospedeSalvo.getProprietarios().add(proprietario);
        proprietario.getHospedes().add(hospedeSalvo);

        // Salva o proprietário para garantir que a tabela de junção seja atualizada
        proprietarioRepository.save(proprietario);

        return hospedeSalvo;
    }

    /**
     * Atualiza um hóspede existente
     *
     * @param id                ID do hóspede a ser atualizado
     * @param hospedeAtualizado Dados atualizados do hóspede
     * @return O hóspede atualizado
     * @throws ResourceNotFoundException se o hóspede não for encontrado
     * @throws BusinessException         se o CPF já estiver em uso por outro
     *                                   hóspede
     */
    @Transactional
    public Hospede atualizar(Long id, Hospede hospedeAtualizado) {
        Hospede hospedeExistente = buscarPorId(id);

        // Atualizar campos básicos
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

    public List<Hospede> listarPorProprietario(Long proprietarioId) {
        Proprietario proprietario = proprietarioRepository.findById(proprietarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Proprietário não encontrado"));
        return new ArrayList<>(proprietario.getHospedes()); // retorna apenas os hóspedes associados
    }

    public Long countHospedesByProprietario(Long proprietarioId) {
        Long totalHospedes = hospedeRepository.countHospedesByProprietarioId(proprietarioId);

        if (totalHospedes == null) {
            throw new ResourceNotFoundException("Proprietário não encontrado ou sem hóspedes");
        }

        return totalHospedes;
    }

}
