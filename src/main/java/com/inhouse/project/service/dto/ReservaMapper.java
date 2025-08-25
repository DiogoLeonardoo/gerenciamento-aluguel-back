package com.inhouse.project.service.dto;

import com.inhouse.project.domain.Reserva;
import com.inhouse.project.domain.Casa;
import com.inhouse.project.domain.Hospede;
import com.inhouse.project.service.CasaService;
import com.inhouse.project.service.HospedeService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReservaMapper {

    private final CasaService casaService;
    private final HospedeService hospedeService;

    public Reserva toEntity(CreateReservaRequest request) {
        Reserva reserva = new Reserva();
        
        // Buscar entidades relacionadas pelos IDs
        Casa casa = casaService.buscarPorId(request.getCasaId());
        Hospede hospedePrincipal = hospedeService.buscarPorId(request.getHospedePrincipalId());
        
        reserva.setCasa(casa);
        reserva.setHospedePrincipal(hospedePrincipal);
        
        // Se houver outros hóspedes, buscá-los também
        if (request.getHospedeIds() != null && !request.getHospedeIds().isEmpty()) {
            List<Hospede> hospedes = request.getHospedeIds().stream()
                    .map(hospedeService::buscarPorId)
                    .collect(Collectors.toList());
            reserva.setHospedes(hospedes);
        }
        
        // Mapear campos simples
        reserva.setDataCheckin(request.getDataCheckin());
        reserva.setDataCheckout(request.getDataCheckout());
        reserva.setNumPessoas(request.getNumPessoas());
        reserva.setObservacoes(request.getObservacoes());
        
        // Valor pago inicial (se fornecido)
        if (request.getValorPago() != null) {
            reserva.setValorPago(request.getValorPago());
        }
        
        return reserva;
    }

    public ReservaResponse toResponse(Reserva reserva) {
        ReservaResponse response = new ReservaResponse();
        
        // Mapear campos simples
        response.setId(reserva.getId());
        response.setDataCheckin(reserva.getDataCheckin());
        response.setDataCheckout(reserva.getDataCheckout());
        response.setNumPessoas(reserva.getNumPessoas());
        response.setValorTotal(reserva.getValorTotal());
        response.setValorPago(reserva.getValorPago());
        response.setStatus(reserva.getStatus());
        response.setObservacoes(reserva.getObservacoes());
        response.setCreatedAt(reserva.getCreatedAt());
        response.setUpdatedAt(reserva.getUpdatedAt());
        
        // Calcular dias de hospedagem
        if (reserva.getDataCheckin() != null && reserva.getDataCheckout() != null) {
            response.setDiasHospedagem(ChronoUnit.DAYS.between(
                reserva.getDataCheckin(), reserva.getDataCheckout()));
        }
        
        // Mapear casa
        if (reserva.getCasa() != null) {
            response.setCasa(mapCasaToResumo(reserva.getCasa()));
        }
        
        // Mapear hóspede principal
        if (reserva.getHospedePrincipal() != null) {
            response.setHospedePrincipal(mapHospedeToResumo(reserva.getHospedePrincipal()));
        }
        
        // Mapear outros hóspedes
        if (reserva.getHospedes() != null && !reserva.getHospedes().isEmpty()) {
            List<ReservaResponse.HospedeResumo> hospedes = reserva.getHospedes().stream()
                    .map(this::mapHospedeToResumo)
                    .collect(Collectors.toList());
            response.setHospedes(hospedes);
        }
        
        return response;
    }

    private ReservaResponse.CasaResumo mapCasaToResumo(Casa casa) {
        ReservaResponse.CasaResumo resumo = new ReservaResponse.CasaResumo();
        resumo.setId(casa.getId());
        resumo.setNome(casa.getNome());
        resumo.setEndereco(casa.getEndereco());
        resumo.setCidade(casa.getCidade());
        resumo.setEstado(casa.getEstado());
        resumo.setCep(casa.getCep());
        resumo.setQuartos(casa.getQuartos());
        resumo.setBanheiros(casa.getBanheiros());
        resumo.setMaxPessoas(casa.getMaxPessoas());
        resumo.setValorDiaria(casa.getValorDiaria());
        resumo.setDescricao(casa.getDescricao());
        return resumo;
    }

    private ReservaResponse.HospedeResumo mapHospedeToResumo(Hospede hospede) {
        ReservaResponse.HospedeResumo resumo = new ReservaResponse.HospedeResumo();
        resumo.setId(hospede.getId());
        resumo.setNome(hospede.getNome());
        resumo.setCpf(hospede.getCpf());
        resumo.setEmail(hospede.getEmail());
        resumo.setTelefone(hospede.getTelefone());
        return resumo;
    }
}