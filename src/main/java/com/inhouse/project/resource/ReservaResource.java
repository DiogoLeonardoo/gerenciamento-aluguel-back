package com.inhouse.project.resource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inhouse.project.domain.Reserva;
import com.inhouse.project.service.ReservaService;
import com.inhouse.project.service.dto.CancelamentoRequest;
import com.inhouse.project.service.dto.CreateReservaRequest;
import com.inhouse.project.service.dto.DisponibilidadeResponse;
import com.inhouse.project.service.dto.FaturamentoResponse;
import com.inhouse.project.service.dto.ReservaMapper;
import com.inhouse.project.service.dto.ReservaResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
@Slf4j
public class ReservaResource {
    private final ReservaService reservaService;
    private final ReservaMapper reservaMapper;

    @GetMapping
    public ResponseEntity<List<ReservaResponse>> listarTodas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        List<Reserva> reservas = reservaService.listarTodas();
        List<ReservaResponse> response = reservas.stream()
                .map(reservaMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponse> buscarPorId(@PathVariable Long id) {

        Reserva reserva = reservaService.buscarPorId(id);
        ReservaResponse response = reservaMapper.toResponse(reserva);

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PROPRIETARIO')")
    @PostMapping
    public ResponseEntity<ReservaResponse> criarReserva(@Valid @RequestBody CreateReservaRequest request) {
        log.info("Proprietário tentando criar reserva para sua casa ID {} e hóspede ID {}",
                request.getCasaId(), request.getHospedePrincipalId());

        Reserva reserva = reservaMapper.toEntity(request);
        Reserva reservaCriada = reservaService.criarReserva(reserva);
        ReservaResponse response = reservaMapper.toResponse(reservaCriada);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/confirmar")
    public ResponseEntity<ReservaResponse> confirmarReserva(@PathVariable Long id) {

        Reserva reserva = reservaService.confirmarReserva(id);
        ReservaResponse response = reservaMapper.toResponse(reserva);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/checkin")
    public ResponseEntity<ReservaResponse> realizarCheckin(@PathVariable Long id) {

        Reserva reserva = reservaService.realizarCheckin(id);
        ReservaResponse response = reservaMapper.toResponse(reserva);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/checkout")
    public ResponseEntity<ReservaResponse> realizarCheckout(@PathVariable Long id) {

        Reserva reserva = reservaService.realizarCheckout(id);
        ReservaResponse response = reservaMapper.toResponse(reserva);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<ReservaResponse> cancelarReserva(
            @PathVariable Long id,
            @Valid @RequestBody CancelamentoRequest request) {

        Reserva reserva = reservaService.cancelarReserva(id, request.getMotivo());
        ReservaResponse response = reservaMapper.toResponse(reserva);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/checkin-hoje")
    public ResponseEntity<List<ReservaResponse>> reservasParaCheckinHoje() {

        List<Reserva> reservas = reservaService.buscarReservasParaCheckinHoje();
        List<ReservaResponse> response = reservas.stream()
                .map(reservaMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/checkout-hoje")
    public ResponseEntity<List<ReservaResponse>> reservasParaCheckoutHoje() {

        List<Reserva> reservas = reservaService.buscarReservasParaCheckoutHoje();
        List<ReservaResponse> response = reservas.stream()
                .map(reservaMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/faturamento")
    public ResponseEntity<FaturamentoResponse> calcularFaturamentoMensal(
            @RequestParam int ano,
            @RequestParam int mes) {

        BigDecimal faturamento = reservaService.calcularFaturamentoMensal(ano, mes);
        FaturamentoResponse response = new FaturamentoResponse(ano, mes, faturamento);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/disponibilidade")
    @io.swagger.v3.oas.annotations.Operation(summary = "Verifica a disponibilidade de uma casa para um período específico", description = "Este endpoint é público e pode ser acessado sem autenticação")
    public ResponseEntity<DisponibilidadeResponse> verificarDisponibilidade(
            @RequestParam Long casaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {

        log.info("Verificando disponibilidade para casa ID {} entre {} e {}",
                casaId, dataInicio, dataFim);

        boolean disponivel = reservaService.verificarDisponibilidade(casaId, dataInicio, dataFim);
        DisponibilidadeResponse response = new DisponibilidadeResponse(casaId, dataInicio, dataFim, disponivel);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/total-mes-anterior/user/{userId}")
    public ResponseEntity<Double> getTotalReservasMesAnteriorPorUser(
            @PathVariable Long userId) {
        Double total = reservaService.getTotalReservasByUser(userId);
        return ResponseEntity.ok(total);
    }

     @GetMapping("/dias-reservados/{casaId}")
    public ResponseEntity<List<LocalDate>> getDiasReservados(@PathVariable Long casaId) {
        List<LocalDate> diasReservados = reservaService.getDiasReservados(casaId);
        return ResponseEntity.ok(diasReservados);
    }
}
