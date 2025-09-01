package com.inhouse.project.service;

import com.inhouse.project.domain.*;
import com.inhouse.project.exceptions.BusinessException;
import com.inhouse.project.exceptions.ResourceNotFoundException;
import com.inhouse.project.repository.CasaRepository;
import com.inhouse.project.repository.FotoCasaRepository;
import com.inhouse.project.service.dto.CasaCreateDTO;
import com.inhouse.project.service.dto.CasaUpdateDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CasaService {

    private final CasaRepository casaRepository;
    private final UsuarioService usuarioService;
    private final FotoCasaRepository fotoCasaRepository;


    public List<Casa> listarCasasAtivas() {
        return casaRepository.findByAtivaTrue();

    }

    public Casa buscarPorId(Long id) {
        return casaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Casa não encontrada com ID: " + id));
    }

    @Transactional
    public Casa salvarCasa(CasaCreateDTO dto) {
        Usuarios usuarioLogado = usuarioService.getUsuarioLogado();

        if (usuarioLogado.getRole() != Usuarios.Role.PROPRIETARIO) {
            throw new BusinessException("Apenas proprietários podem cadastrar casas");
        }

        Proprietario proprietario = usuarioService.getProprietarioDoUsuario(usuarioLogado.getId());

        // Verificações adicionais de segurança para valores nulos
        // Validação para campos numéricos
        if (dto.getBanheiros() == null) {
            throw new BusinessException("Número de banheiros é obrigatório");
        }
        if (dto.getQuartos() == null) {
            throw new BusinessException("Número de quartos é obrigatório");
        }
        if (dto.getMaxPessoas() == null) {
            throw new BusinessException("Capacidade máxima é obrigatória");
        }
        if (dto.getValorDiaria() == null) {
            throw new BusinessException("Valor da diária é obrigatório");
        }
        
        // Validações para campos de texto
        if (dto.getNome() == null || dto.getNome().trim().isEmpty()) {
            throw new BusinessException("Nome da casa é obrigatório");
        }
        if (dto.getEndereco() == null || dto.getEndereco().trim().isEmpty()) {
            throw new BusinessException("Endereço é obrigatório");
        }
        if (dto.getCidade() == null || dto.getCidade().trim().isEmpty()) {
            throw new BusinessException("Cidade é obrigatória");
        }
        if (dto.getEstado() == null || dto.getEstado().trim().isEmpty()) {
            throw new BusinessException("Estado é obrigatório");
        }
        if (dto.getEstado() != null && dto.getEstado().length() > 2) {
            throw new BusinessException("Estado deve ter no máximo 2 caracteres (sigla do estado)");
        }

        Casa casa = new Casa();
        casa.setNome(dto.getNome());
        casa.setDescricao(dto.getDescricao());
        casa.setEndereco(dto.getEndereco());
        casa.setCidade(dto.getCidade());
        casa.setEstado(dto.getEstado());
        casa.setCep(dto.getCep());
        casa.setQuartos(dto.getQuartos());
        casa.setBanheiros(dto.getBanheiros());
        casa.setMaxPessoas(dto.getMaxPessoas());
        casa.setValorDiaria(dto.getValorDiaria());
        casa.setAtiva(true);
        casa.setProprietario(proprietario);

        Casa casaSalva = casaRepository.save(casa);

        if (dto.getFotos() != null && !dto.getFotos().isEmpty()) {
            // Verificar se há mais de 10 fotos
            if (dto.getFotos().size() > 10) {
                throw new BusinessException("Não é permitido adicionar mais de 10 fotos por casa. Você enviou " + dto.getFotos().size() + " fotos.");
            }
            
            // Contador de fotos processadas
            int fotosProcessadas = 0;
            
            for (var file : dto.getFotos()) {
                if (file != null && !file.isEmpty()) {
                    if (fotosProcessadas >= 10) {
                        log.warn("Limite de 10 fotos atingido para a casa ID: {}. Fotos excedentes ignoradas.", casaSalva.getId());
                        break;
                    }
                    
                    FotoCasa foto = new FotoCasa();
                    foto.setCasa(casaSalva);
                    try {
                        foto.setConteudoArquivo(file.getBytes());
                    } catch (IOException e) {
                        throw new BusinessException("Erro ao ler arquivo: " + file.getOriginalFilename());
                    }
                    foto.setNomeArquivo(file.getOriginalFilename());
                    foto.setContentType(file.getContentType());
                    foto.setTamanho(file.getSize());
                    
                    // A primeira foto é marcada como principal
                    foto.setPrincipal(fotosProcessadas == 0);
                    
                    fotoCasaRepository.save(foto);
                    fotosProcessadas++;
                }
            }
        }

        return casaSalva;
    }


    public Casa atualizar(Long id, Casa casaAtualizada) {
        Casa casaExistente = buscarPorId(id);

        validarCasa(casaAtualizada);

        casaExistente.setNome(casaAtualizada.getNome());
        casaExistente.setDescricao(casaAtualizada.getDescricao());
        casaExistente.setEndereco(casaAtualizada.getEndereco());
        casaExistente.setCidade(casaAtualizada.getCidade());
        casaExistente.setEstado(casaAtualizada.getEstado());
        casaExistente.setCep(casaAtualizada.getCep());
        casaExistente.setQuartos(casaAtualizada.getQuartos());
        casaExistente.setBanheiros(casaAtualizada.getBanheiros());
        casaExistente.setMaxPessoas(casaAtualizada.getMaxPessoas());
        casaExistente.setValorDiaria(casaAtualizada.getValorDiaria());

        return casaRepository.save(casaExistente);
    }

    @Transactional
    public void inativar(Long id) {
        Casa casa = buscarPorId(id);
        
        // Verifica se a casa já está inativa
        if (Boolean.FALSE.equals(casa.getAtiva())) {
            log.info("Casa já está inativa: {}", casa.getNome());
            return;
        }

        // Inativa a casa
        casa.setAtiva(false);
        
        // Inativa todas as reservas futuras associadas à casa
        if (casa.getReservas() != null) {
            for (Reserva reserva : casa.getReservas()) {
                // Só inativa reservas futuras ou em andamento (não altera reservas passadas)
                if (reserva.getDataCheckin().isAfter(LocalDate.now()) || 
                    (reserva.getDataCheckin().isBefore(LocalDate.now()) && 
                     reserva.getDataCheckout().isAfter(LocalDate.now()))) {
                    
                    // Define o status como CANCELADA
                    reserva.setStatus(Reserva.StatusReserva.CANCELADA);
                    log.info("Reserva {} cancelada devido à inativação da casa", reserva.getId());
                }
            }
        }
        
        // Persiste as alterações
        casaRepository.save(casa);
        log.info("Casa inativada com sucesso: {}", casa.getNome());
    }

      @Transactional
        public void ativar(Long id) {
            Casa casa = buscarPorId(id);
            casa.setAtiva(true);
        }

    public List<Casa> buscarCasasDisponiveis(LocalDate checkin, LocalDate checkout,
                                             String cidade, Integer pessoas,
                                             BigDecimal minValor, BigDecimal maxValor) {

        validarPeriodoReserva(checkin, checkout);

        return casaRepository.findCasasComFiltros(cidade, pessoas, minValor, maxValor, checkin, checkout);
    }

    public Casa adicionarItemInventario(Long casaId, InventarioCasa item) {
        Casa casa = buscarPorId(casaId);

        item.setCasa(casa);
        casa.getInventario().add(item);

        return casaRepository.save(casa);
    }

    private void validarCasa(Casa casa) {
        if (casa.getNome() == null || casa.getNome().trim().isEmpty()) {
            throw new BusinessException("Nome da casa é obrigatório");
        }

        if (casa.getEndereco() == null || casa.getEndereco().trim().isEmpty()) {
            throw new BusinessException("Endereço é obrigatório");
        }

        if (casa.getValorDiaria() == null || casa.getValorDiaria().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Valor da diária deve ser maior que zero");
        }

        if (casa.getMaxPessoas() == null || casa.getMaxPessoas() <= 0) {
            throw new BusinessException("Capacidade máxima deve ser maior que zero");
        }

        if (casa.getQuartos() == null || casa.getQuartos() <= 0) {
            throw new BusinessException("Número de quartos deve ser maior que zero");
        }

        if (casa.getBanheiros() == null || casa.getBanheiros() <= 0) {
            throw new BusinessException("Número de banheiros deve ser maior que zero");
        }
    }

    private void validarPeriodoReserva(LocalDate checkin, LocalDate checkout) {
        if (checkin == null || checkout == null) {
            throw new BusinessException("Datas de check-in e check-out são obrigatórias");
        }

        if (checkin.isBefore(LocalDate.now())) {
            throw new BusinessException("Data de check-in não pode ser anterior à data atual");
        }

        if (checkout.isBefore(checkin.plusDays(1))) {
            throw new BusinessException("Data de check-out deve ser pelo menos 1 dia após o check-in");
        }
    }

    @Transactional
    public Casa atualizarParcial(Long id, CasaUpdateDTO dto) {
        Casa casaExistente = buscarPorId(id);

        // Atualiza campos básicos
        if (dto.getNome() != null) casaExistente.setNome(dto.getNome());
        if (dto.getDescricao() != null) casaExistente.setDescricao(dto.getDescricao());
        if (dto.getQuartos() != null) casaExistente.setQuartos(dto.getQuartos());
        if (dto.getBanheiros() != null) casaExistente.setBanheiros(dto.getBanheiros());
        if (dto.getMaxPessoas() != null) casaExistente.setMaxPessoas(dto.getMaxPessoas());
        if (dto.getValorDiaria() != null) casaExistente.setValorDiaria(dto.getValorDiaria());

        // Remove fotos existentes
        if (dto.getFotosExcluir() != null) {
            dto.getFotosExcluir().forEach(fotoId -> {
                FotoCasa foto = fotoCasaRepository.findById(fotoId)
                        .orElseThrow(() -> new BusinessException("Foto não encontrada: " + fotoId));
                casaExistente.getFotos().remove(foto);
                fotoCasaRepository.delete(foto);
            });
        }

        // Adiciona novas fotos
        if (dto.getFotosNovas() != null) {
            dto.getFotosNovas().forEach(multipartFile -> {
                FotoCasa novaFoto = new FotoCasa();
                novaFoto.setCasa(casaExistente);

                try {
                    // Salva conteúdo direto no banco
                    novaFoto.setConteudoArquivo(multipartFile.getBytes());
                    novaFoto.setNomeArquivo(multipartFile.getOriginalFilename());
                } catch (IOException e) {
                    throw new BusinessException("Erro ao ler arquivo: " + multipartFile.getOriginalFilename());
                }

                novaFoto.setPrincipal(false);
                casaExistente.getFotos().add(novaFoto);
                fotoCasaRepository.save(novaFoto);
            });
        }

        return casaRepository.save(casaExistente);
    }

    public List<Casa> listarCasasDoProprietario() {
        // Pega o usuário logado
        Usuarios usuarioLogado = usuarioService.getUsuarioLogado();

        // Verifica se é proprietário
        if (usuarioLogado.getRole() != Usuarios.Role.PROPRIETARIO) {
            throw new BusinessException("Usuário não é proprietário");
        }

        // Retorna apenas casas do proprietário logado
        return casaRepository.findByProprietarioUsuarioId(usuarioLogado.getId());
    }

}
