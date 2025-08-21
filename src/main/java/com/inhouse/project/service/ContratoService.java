package com.inhouse.project.service;

import com.inhouse.project.domain.Contrato;
import com.inhouse.project.domain.Reserva;
import com.inhouse.project.domain.TemplateContrato;
import com.inhouse.project.exceptions.BusinessException;
import com.inhouse.project.repository.ContratoRepository;
import com.inhouse.project.repository.TemplateContratoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ContratoService {

    private final ContratoRepository contratoRepository;
    private final TemplateContratoRepository templateContratoRepository;
    private ReservaService reservaService;

    @Autowired
    public void setReservaService(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    public Contrato gerarContratoParaReserva(Long reservaId) {
        Reserva reserva = reservaService.buscarPorId(reservaId);

        // Buscar template padrão
        TemplateContrato template = templateContratoRepository.findByAtivoTrueOrderByCreatedAtDesc()
                .stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException("Nenhum template de contrato ativo encontrado"));

        String conteudoContrato = preencherTemplate(template.getConteudo(), reserva);

        Contrato contrato = new Contrato();
        contrato.setReserva(reserva);
        contrato.setTemplate(template);
        contrato.setConteudoContrato(conteudoContrato);
        contrato.setAssinado(false);

        Contrato contratoSalvo = contratoRepository.save(contrato);

        log.info("Contrato gerado para reserva ID: {}", reservaId);

        return contratoSalvo;
    }

    private String preencherTemplate(String template, Reserva reserva) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Substituir placeholders no template
        String conteudo = template
                .replace("{nome_proprietario}", reserva.getCasa().getProprietario().getUsuario().getNome())
                .replace("{nome_hospede_principal}", reserva.getHospedePrincipal().getNome())
                .replace("{cpf_hospede_principal}", reserva.getHospedePrincipal().getCpf())
                .replace("{nome_casa}", reserva.getCasa().getNome())
                .replace("{endereco_casa}", reserva.getCasa().getEndereco())
                .replace("{data_checkin}", reserva.getDataCheckin().format(formatter))
                .replace("{data_checkout}", reserva.getDataCheckout().format(formatter))
                .replace("{valor_total}", "R$ " + reserva.getValorTotal().toString())
                .replace("{valor_diaria}", "R$ " + reserva.getCasa().getValorDiaria().toString());

        // Adicionar lista de hóspedes
        if (reserva.getHospedes() != null && !reserva.getHospedes().isEmpty()) {
            String listaHospedes = reserva.getHospedes().stream()
                    .map(h -> "- " + h.getNome() + " (CPF: " + h.getCpf() + ")")
                    .collect(Collectors.joining("\n"));
            conteudo = conteudo.replace("{lista_hospedes}", listaHospedes);
        } else {
            conteudo = conteudo.replace("{lista_hospedes}", "- " + reserva.getHospedePrincipal().getNome() +
                    " (CPF: " + reserva.getHospedePrincipal().getCpf() + ")");
        }

        // Adicionar inventário
        if (reserva.getCasa().getInventario() != null && !reserva.getCasa().getInventario().isEmpty()) {
            String inventario = reserva.getCasa().getInventario().stream()
                    .map(i -> "- " + i.getItem() + " (" + i.getQuantidade() + "x) - " + i.getCondicao())
                    .collect(Collectors.joining("\n"));
            conteudo = conteudo.replace("{inventario}", inventario);
        } else {
            conteudo = conteudo.replace("{inventario}", "Inventário não disponível");
        }

        return conteudo;
    }
}
