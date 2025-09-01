package com.inhouse.project.resource;

import com.inhouse.project.domain.InventarioCasa;
import com.inhouse.project.repository.ProprietarioRepository;
import com.inhouse.project.service.InventarioCasaService;
import com.inhouse.project.service.dto.InventarioItemDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proprietario")
@RequiredArgsConstructor
@Slf4j
public class ProprietarioResource {

    private final ProprietarioRepository proprietarioRepository;

    @GetMapping("/{userId}")
    @Operation(summary = "Lista o número de casas de um proprietário")
    @ApiResponse(responseCode = "200", description = "Número de casas recuperadas com sucesso")
    @ApiResponse(responseCode = "404", description = "Não encontrado")
    public ResponseEntity<Long> quantidadeCasas (@PathVariable Long userId) {
        Long qtd = proprietarioRepository.countByUserId(userId);
        return ResponseEntity.ok(qtd);
    }
}
