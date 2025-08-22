package com.inhouse.project.resource;

import com.inhouse.project.domain.InventarioCasa;
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
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class InventarioCasaResource {

    private final InventarioCasaService inventarioCasaService;

    @PostMapping("/casa/{casaId}")
    @PreAuthorize("hasRole('PROPRIETARIO') or hasRole('ADMIN')")
    @Operation(summary = "Adiciona um novo item ao inventário de uma casa")
    @ApiResponse(responseCode = "201", description = "Item adicionado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "403", description = "Sem permissão para adicionar itens")
    @ApiResponse(responseCode = "404", description = "Casa não encontrada")
    public ResponseEntity<InventarioCasa> adicionarItem(
            @PathVariable Long casaId,
            @Valid @RequestBody InventarioItemDTO itemDTO) {
        InventarioCasa itemSalvo = inventarioCasaService.adicionarItem(casaId, itemDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(itemSalvo);
    }
    
    @PostMapping("/casa/{casaId}/multiplos")
    @PreAuthorize("hasRole('PROPRIETARIO') or hasRole('ADMIN')")
    @Operation(summary = "Adiciona múltiplos itens ao inventário de uma casa")
    @ApiResponse(responseCode = "201", description = "Itens adicionados com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "403", description = "Sem permissão para adicionar itens")
    @ApiResponse(responseCode = "404", description = "Casa não encontrada")
    public ResponseEntity<List<InventarioCasa>> adicionarMultiplosItens(
            @PathVariable Long casaId,
            @Valid @RequestBody List<InventarioItemDTO> itensDTO) {
        
        List<InventarioCasa> itensSalvos = itensDTO.stream()
            .map(item -> inventarioCasaService.adicionarItem(casaId, item))
            .toList();
            
        return ResponseEntity.status(HttpStatus.CREATED).body(itensSalvos);
    }

    @GetMapping("/casa/{casaId}")
    @Operation(summary = "Lista todos os itens do inventário de uma casa")
    @ApiResponse(responseCode = "200", description = "Lista de itens recuperada com sucesso")
    @ApiResponse(responseCode = "404", description = "Casa não encontrada")
    public ResponseEntity<List<InventarioCasa>> listarItensPorCasa(@PathVariable Long casaId) {
        List<InventarioCasa> itens = inventarioCasaService.listarItensPorCasa(casaId);
        return ResponseEntity.ok(itens);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um item específico do inventário pelo ID")
    @ApiResponse(responseCode = "200", description = "Item encontrado")
    @ApiResponse(responseCode = "404", description = "Item não encontrado")
    public ResponseEntity<InventarioCasa> buscarItemPorId(@PathVariable Long id) {
        InventarioCasa item = inventarioCasaService.buscarItemPorId(id);
        return ResponseEntity.ok(item);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROPRIETARIO') or hasRole('ADMIN')")
    @Operation(summary = "Atualiza um item do inventário")
    @ApiResponse(responseCode = "200", description = "Item atualizado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "403", description = "Sem permissão para atualizar o item")
    @ApiResponse(responseCode = "404", description = "Item não encontrado")
    public ResponseEntity<InventarioCasa> atualizarItem(
            @PathVariable Long id,
            @Valid @RequestBody InventarioItemDTO itemDTO) {
        InventarioCasa itemAtualizado = inventarioCasaService.atualizarItem(id, itemDTO);
        return ResponseEntity.ok(itemAtualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PROPRIETARIO') or hasRole('ADMIN')")
    @Operation(summary = "Remove um item do inventário")
    @ApiResponse(responseCode = "204", description = "Item removido com sucesso")
    @ApiResponse(responseCode = "403", description = "Sem permissão para remover o item")
    @ApiResponse(responseCode = "404", description = "Item não encontrado")
    public ResponseEntity<Void> removerItem(@PathVariable Long id) {
        inventarioCasaService.removerItem(id);
        return ResponseEntity.noContent().build();
    }
}
