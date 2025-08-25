package com.inhouse.project.service;

import com.inhouse.project.domain.Proprietario;
import com.inhouse.project.domain.Reserva;
import com.inhouse.project.domain.Usuarios;
import com.inhouse.project.exceptions.BusinessException;
import com.inhouse.project.exceptions.ResourceNotFoundException;
import com.inhouse.project.repository.ReservaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final CasaService casaService;
    private final HospedeService hospedeService;
    private final ContratoHelper contratoHelper;
    private final UsuarioService usuarioService;

    public List<Reserva> listarTodas() {
        return reservaRepository.findAll();
    }

    public Reserva buscarPorId(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva não encontrada com ID: " + id));
    }

    public Reserva criarReserva(Reserva reserva) {
        validarReserva(reserva);
        
        // Verificar se o usuário logado é o proprietário da casa
        Usuarios usuarioLogado = usuarioService.getUsuarioLogado();
        
        if (usuarioLogado.getRole() != Usuarios.Role.PROPRIETARIO) {
            throw new BusinessException("Apenas proprietários podem realizar reservas");
        }
        
        // Obter o proprietário do usuário logado
        Proprietario proprietarioLogado = usuarioService.getProprietarioDoUsuario(usuarioLogado.getId());
        
        // Verificar se a casa pertence ao proprietário logado
        if (reserva.getCasa().getProprietario() == null || 
            !reserva.getCasa().getProprietario().getId().equals(proprietarioLogado.getId())) {
            throw new BusinessException("Você só pode realizar reservas para suas próprias casas");
        }

        // Verificar disponibilidade da casa
        if (reservaRepository.existeReservaNoPeriodo(reserva.getCasa().getId(),
                reserva.getDataCheckin(),
                reserva.getDataCheckout())) {
            throw new BusinessException("Casa não disponível no período selecionado");
        }

        // Calcular valor total
        long dias = ChronoUnit.DAYS.between(reserva.getDataCheckin(), reserva.getDataCheckout());
        BigDecimal valorTotal = reserva.getCasa().getValorDiaria().multiply(BigDecimal.valueOf(dias));
        reserva.setValorTotal(valorTotal);
        reserva.setValorPago(BigDecimal.ZERO);
        reserva.setStatus(Reserva.StatusReserva.PENDENTE);

        Reserva reservaSalva = reservaRepository.save(reserva);

        // Gerar contrato automaticamente
        contratoHelper.gerarContrato(reservaSalva);

        log.info("Reserva criada - ID: {}, Casa: {}, Período: {} a {}",
                reservaSalva.getId(), reserva.getCasa().getNome(),
                reserva.getDataCheckin(), reserva.getDataCheckout());

        return reservaSalva;
    }

    public Reserva confirmarReserva(Long id) {
        Reserva reserva = buscarPorId(id);

        if (reserva.getStatus() != Reserva.StatusReserva.PENDENTE) {
            throw new BusinessException("Apenas reservas pendentes podem ser confirmadas");
        }

        // Verificar se ainda está disponível
        if (reservaRepository.existeReservaNoPeriodo(reserva.getCasa().getId(),
                reserva.getDataCheckin(),
                reserva.getDataCheckout())) {
            throw new BusinessException("Casa não está mais disponível no período");
        }

        reserva.setStatus(Reserva.StatusReserva.CONFIRMADA);

        log.info("Reserva confirmada - ID: {}", id);
        return reservaRepository.save(reserva);
    }

    public Reserva realizarCheckin(Long id) {
        Reserva reserva = buscarPorId(id);

        if (reserva.getStatus() != Reserva.StatusReserva.CONFIRMADA) {
            throw new BusinessException("Apenas reservas confirmadas podem fazer check-in");
        }

        if (reserva.getDataCheckin().isAfter(LocalDate.now())) {
            throw new BusinessException("Data de check-in ainda não chegou");
        }

        // Verificar se o pagamento foi realizado (regra de negócio)
        if (reserva.getValorPago().compareTo(reserva.getValorTotal()) < 0) {
            throw new BusinessException("Pagamento deve estar completo para realizar check-in");
        }

        reserva.setStatus(Reserva.StatusReserva.CHECKIN);

        log.info("Check-in realizado - Reserva ID: {}", id);
        return reservaRepository.save(reserva);
    }

    public Reserva realizarCheckout(Long id) {
        Reserva reserva = buscarPorId(id);

        if (reserva.getStatus() != Reserva.StatusReserva.CHECKIN) {
            throw new BusinessException("Apenas reservas com check-in podem fazer check-out");
        }

        reserva.setStatus(Reserva.StatusReserva.CHECKOUT);

        log.info("Check-out realizado - Reserva ID: {}", id);
        return reservaRepository.save(reserva);
    }

    public Reserva cancelarReserva(Long id, String motivo) {
        Reserva reserva = buscarPorId(id);

        if (reserva.getStatus() == Reserva.StatusReserva.CHECKIN) {
            throw new BusinessException("Não é possível cancelar reserva com check-in já realizado");
        }

        // Política de cancelamento: até 7 dias antes sem multa
        long diasAteCheckin = ChronoUnit.DAYS.between(LocalDate.now(), reserva.getDataCheckin());
        if (diasAteCheckin < 7 && reserva.getValorPago().compareTo(BigDecimal.ZERO) > 0) {
            // Aplicar multa de 20%
            BigDecimal multa = reserva.getValorPago().multiply(BigDecimal.valueOf(0.2));
            log.info("Multa aplicada por cancelamento: R$ {} - Reserva ID: {}", multa, id);
        }

        reserva.setStatus(Reserva.StatusReserva.CANCELADA);
        reserva.setObservacoes(reserva.getObservacoes() + "\nMotivo cancelamento: " + motivo);

        log.info("Reserva cancelada - ID: {}, Motivo: {}", id, motivo);
        return reservaRepository.save(reserva);
    }

    public List<Reserva> buscarReservasParaCheckinHoje() {
        return reservaRepository.findReservasParaCheckinHoje(LocalDate.now());
    }

    public List<Reserva> buscarReservasParaCheckoutHoje() {
        return reservaRepository.findReservasParaCheckoutHoje(LocalDate.now());
    }

    public BigDecimal calcularFaturamentoMensal(int ano, int mes) {
        LocalDate inicio = LocalDate.of(ano, mes, 1);
        LocalDate fim = inicio.withDayOfMonth(inicio.lengthOfMonth());

        return reservaRepository.calcularFaturamentoPeriodo(inicio, fim);
    }

    private void validarReserva(Reserva reserva) {
        if (reserva.getCasa() == null) {
            throw new BusinessException("Casa é obrigatória");
        }

        if (reserva.getHospedePrincipal() == null) {
            throw new BusinessException("Hóspede principal é obrigatório");
        }

        if (reserva.getDataCheckin() == null || reserva.getDataCheckout() == null) {
            throw new BusinessException("Datas de check-in e check-out são obrigatórias");
        }

        if (reserva.getDataCheckin().isBefore(LocalDate.now())) {
            throw new BusinessException("Data de check-in não pode ser anterior à data atual");
        }

        if (reserva.getDataCheckout().isBefore(reserva.getDataCheckin().plusDays(1))) {
            throw new BusinessException("Data de check-out deve ser pelo menos 1 dia após o check-in");
        }

        if (reserva.getNumPessoas() == null || reserva.getNumPessoas() <= 0) {
            throw new BusinessException("Número de pessoas deve ser maior que zero");
        }

        if (reserva.getNumPessoas() > reserva.getCasa().getMaxPessoas()) {
            throw new BusinessException("Número de pessoas excede a capacidade da casa");
        }

        // Verificar se todos os hóspedes estão na lista
        if (reserva.getHospedes() != null && reserva.getHospedes().size() > reserva.getNumPessoas()) {
            throw new BusinessException("Número de hóspedes não pode ser maior que o número de pessoas da reserva");
        }
    }
    
    /**
     * Verifica se uma casa está disponível para o período especificado
     * Este é um endpoint público que pode ser acessado sem autenticação.
     * 
     * @param casaId ID da casa
     * @param dataInicio Data de início do período
     * @param dataFim Data de fim do período
     * @return true se a casa estiver disponível, false caso contrário
     */
    public boolean verificarDisponibilidade(Long casaId, LocalDate dataInicio, LocalDate dataFim) {
        try {
            // Verificações básicas
            if (casaId == null || dataInicio == null || dataFim == null) {
                return false;
            }
            
            // Validações de período
            if (dataInicio.isBefore(LocalDate.now())) {
                return false;
            }
            
            if (dataFim.isBefore(dataInicio) || dataFim.isEqual(dataInicio)) {
                return false;
            }
            
            try {
                // Verifica se a casa existe silenciosamente
                casaService.buscarPorId(casaId);
            } catch (ResourceNotFoundException e) {
                // Se a casa não existir, consideramos que não está disponível
                log.debug("Casa não encontrada ao verificar disponibilidade: {}", casaId);
                return false;
            }
        
            // Verifica se não há reservas confirmadas ou em check-in para o período
            List<Reserva> reservasExistentes = reservaRepository.findByCasaIdAndStatusIn(
                casaId,
                List.of(Reserva.StatusReserva.CONFIRMADA, Reserva.StatusReserva.CHECKIN)
            );
            
            // Verifica se alguma reserva existente se sobrepõe ao período solicitado
            for (Reserva reserva : reservasExistentes) {
                // Verifica sobreposição de datas
                if ((dataInicio.isBefore(reserva.getDataCheckout()) || dataInicio.isEqual(reserva.getDataCheckout())) &&
                    (dataFim.isAfter(reserva.getDataCheckin()) || dataFim.isEqual(reserva.getDataCheckin()))) {
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            log.error("Erro ao verificar disponibilidade da casa {}: {}", casaId, e.getMessage());
            return false;
        }
    }
}
