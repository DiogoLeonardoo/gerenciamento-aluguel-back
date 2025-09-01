package com.inhouse.project.resource;

import com.inhouse.project.domain.Hospede;
import com.inhouse.project.service.HospedeService;
import com.inhouse.project.service.dto.HospedeDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/hospedes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Hóspedes", description = "API para gerenciamento de hóspedes")
public class HospedeResource {

    private final HospedeService hospedeService;

    /**
     * Converte DTO para entidade
     */
    private Hospede toEntity(HospedeDTO dto) {
        Hospede hospede = new Hospede();
        hospede.setId(dto.getId());
        hospede.setNome(dto.getNome());
        hospede.setCpf(dto.getCpf());
        hospede.setRg(dto.getRg());
        hospede.setTelefone(dto.getTelefone());
        hospede.setEmail(dto.getEmail());
        hospede.setEndereco(dto.getEndereco());
        hospede.setCidade(dto.getCidade());
        hospede.setEstado(dto.getEstado());
        hospede.setCep(dto.getCep());
        hospede.setDataNascimento(dto.getDataNascimento());
        return hospede;
    }

    /**
     * Converte entidade para DTO
     */
    private HospedeDTO toDto(Hospede hospede) {
        HospedeDTO dto = new HospedeDTO();
        dto.setId(hospede.getId());
        dto.setNome(hospede.getNome());
        dto.setCpf(hospede.getCpf());
        dto.setRg(hospede.getRg());
        dto.setTelefone(hospede.getTelefone());
        dto.setEmail(hospede.getEmail());
        dto.setEndereco(hospede.getEndereco());
        dto.setCidade(hospede.getCidade());
        dto.setEstado(hospede.getEstado());
        dto.setCep(hospede.getCep());
        dto.setDataNascimento(hospede.getDataNascimento());
        return dto;
    }

    @GetMapping
    @Operation(summary = "Lista todos os hóspedes do proprietário")
    @ApiResponse(responseCode = "200", description = "Hóspedes listados com sucesso")
    public ResponseEntity<List<HospedeDTO>> listarTodos(@RequestParam Long proprietarioId) {
        // Busca apenas os hóspedes associados ao proprietário
        List<Hospede> hospedes = hospedeService.listarPorProprietario(proprietarioId);

        List<HospedeDTO> dtos = hospedes.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um hóspede pelo ID")
    @ApiResponse(responseCode = "200", description = "Hóspede encontrado")
    @ApiResponse(responseCode = "404", description = "Hóspede não encontrado")
    public ResponseEntity<HospedeDTO> buscarPorId(@PathVariable Long id) {
        Hospede hospede = hospedeService.buscarPorId(id);
        return ResponseEntity.ok(toDto(hospede));
    }

    @GetMapping("/cpf/{cpf}")
    @Operation(summary = "Busca um hóspede pelo CPF")
    @ApiResponse(responseCode = "200", description = "Hóspede encontrado")
    @ApiResponse(responseCode = "404", description = "Hóspede não encontrado")
    public ResponseEntity<HospedeDTO> buscarPorCpf(@PathVariable String cpf) {
        Hospede hospede = hospedeService.buscarPorCpf(cpf);
        return ResponseEntity.ok(toDto(hospede));
    }

    @PostMapping
    @Operation(summary = "Cadastra um novo hóspede e associa ao proprietário")
    @ApiResponse(responseCode = "201", description = "Hóspede cadastrado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROPRIETARIO')")
    public ResponseEntity<HospedeDTO> criar(
            @Valid @RequestBody HospedeDTO dto,
            @RequestParam Long proprietarioId) {
        log.info("Criando novo hóspede com CPF: {} para o proprietário ID: {}", dto.getCpf(), proprietarioId);

        Hospede hospede = toEntity(dto);
        Hospede hospedeSalvo = hospedeService.salvar(hospede, proprietarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(hospedeSalvo));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um hóspede existente")
    @ApiResponse(responseCode = "200", description = "Hóspede atualizado com sucesso")
    @ApiResponse(responseCode = "404", description = "Hóspede não encontrado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROPRIETARIO')")
    public ResponseEntity<HospedeDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody HospedeDTO dto) {

        log.info("Atualizando hóspede com ID: {}", id);

        // Converte DTO para entidade
        Hospede hospede = toEntity(dto);

        // Atualiza somente os dados do hóspede, sem mexer nos proprietários
        Hospede hospedeAtualizado = hospedeService.atualizar(id, hospede);

        return ResponseEntity.ok(toDto(hospedeAtualizado));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um hóspede")
    @ApiResponse(responseCode = "204", description = "Hóspede removido com sucesso")
    @ApiResponse(responseCode = "404", description = "Hóspede não encontrado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        log.info("Excluindo hóspede com ID: {}", id);
        hospedeService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/hospedes-count/{proprietarioId}")
    public ResponseEntity<Long> getTotalHospedesByProprietario(@PathVariable("proprietarioId") Long proprietarioId) {
        Long totalHospedes = hospedeService.countHospedesByProprietario(proprietarioId);
        return ResponseEntity.ok(totalHospedes);
    }
}
