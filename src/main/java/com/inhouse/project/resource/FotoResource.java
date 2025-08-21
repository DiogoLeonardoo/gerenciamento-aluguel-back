package com.inhouse.project.resource;

import com.inhouse.project.domain.FotoCasa;
import com.inhouse.project.exceptions.ResourceNotFoundException;
import com.inhouse.project.service.FotoCasaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fotos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class FotoResource {

    private final FotoCasaService fotoCasaService;

    @PostMapping("/upload/casa/{casaId}")
    @Operation(summary = "Faz upload de uma foto para uma casa específica")
    public ResponseEntity<Map<String, Object>> uploadFoto(
            @PathVariable Long casaId,
            @RequestParam("file") MultipartFile file) throws IOException {
        
        FotoCasa foto = fotoCasaService.salvarFoto(casaId, file);
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", foto.getId());
        response.put("nome", foto.getNomeArquivo());
        response.put("url", "/api/fotos/" + foto.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recupera uma foto pelo ID")
    @ApiResponse(responseCode = "200", description = "Foto encontrada", 
                 content = @Content(mediaType = "image/*"))
    @ApiResponse(responseCode = "404", description = "Foto não encontrada")
    public ResponseEntity<byte[]> getFoto(@PathVariable Long id) {
        FotoCasa foto = fotoCasaService.getFotoById(id);
        
        // Determinar o tipo de conteúdo
        String contentType = foto.getContentType();
        if (contentType == null) {
            contentType = "image/jpeg"; // Padrão
            if (foto.getNomeArquivo() != null) {
                String nome = foto.getNomeArquivo().toLowerCase();
                if (nome.endsWith(".png")) {
                    contentType = "image/png";
                } else if (nome.endsWith(".gif")) {
                    contentType = "image/gif";
                } else if (nome.endsWith(".bmp")) {
                    contentType = "image/bmp";
                } else if (nome.endsWith(".webp")) {
                    contentType = "image/webp";
                }
            }
        }
        
        // Criar cabeçalhos
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        
        // Adicionar Cache-Control
        headers.setCacheControl("max-age=86400"); // 1 dia de cache
        
        // Retornar a resposta com o conteúdo da imagem
        return new ResponseEntity<>(foto.getConteudoArquivo(), headers, HttpStatus.OK);
    }
    
    @GetMapping("/casa/{casaId}/principal")
    @Operation(summary = "Recupera a foto principal de uma casa")
    @ApiResponse(responseCode = "200", description = "Foto encontrada", 
                 content = @Content(mediaType = "image/*"))
    @ApiResponse(responseCode = "404", description = "Foto não encontrada")
    public ResponseEntity<byte[]> getPrimeiraFotoCasa(@PathVariable Long casaId) {
        // Buscar todas as fotos da casa
        List<FotoCasa> fotos = fotoCasaService.getFotosByCasaId(casaId);
        
        if (fotos.isEmpty()) {
            throw new ResourceNotFoundException("Nenhuma foto encontrada para a casa com ID: " + casaId);
        }
        
        // Encontrar a foto principal ou pegar a primeira
        FotoCasa fotoPrincipal = fotos.stream()
            .filter(f -> Boolean.TRUE.equals(f.getPrincipal()))
            .findFirst()
            .orElse(fotos.get(0));
        
        // Determinar o tipo de conteúdo
        String contentType = fotoPrincipal.getContentType();
        if (contentType == null) {
            contentType = "image/jpeg"; // Padrão
        }
        
        // Criar cabeçalhos
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setCacheControl("max-age=86400"); // 1 dia de cache
        
        // Retornar a resposta
        return new ResponseEntity<>(fotoPrincipal.getConteudoArquivo(), headers, HttpStatus.OK);
    }
    
    /**
     * Lista todas as fotos disponíveis para uma casa específica
     */
    @GetMapping("/lista/casa/{casaId}")
    @Operation(summary = "Lista todas as fotos de uma casa")
    public ResponseEntity<List<FotoInfoDTO>> listarFotosDaCasa(@PathVariable Long casaId) {
        // Buscar todas as fotos da casa
        List<FotoCasa> fotos = fotoCasaService.getFotosByCasaId(casaId);
        
        if (fotos.isEmpty()) {
            throw new ResourceNotFoundException("Nenhuma foto encontrada para a casa com ID: " + casaId);
        }
        
        // Converter para DTOs para evitar serialização de dados binários
        List<FotoInfoDTO> fotosInfo = fotos.stream()
            .map(foto -> new FotoInfoDTO(
                foto.getId(),
                foto.getNomeArquivo(),
                foto.getDescricao(),
                foto.getPrincipal(),
                "/api/fotos/" + foto.getId(),
                foto.getContentType(),
                foto.getTamanho()
            ))
            .collect(java.util.stream.Collectors.toList());
        
        return ResponseEntity.ok(fotosInfo);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Remove uma foto pelo ID")
    public ResponseEntity<Void> deleteFoto(@PathVariable Long id) {
        fotoCasaService.deleteFoto(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * DTO para retornar informações sobre as fotos sem incluir o conteúdo binário
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class FotoInfoDTO {
        private Long id;
        private String nomeArquivo;
        private String descricao;
        private Boolean principal;
        private String url;
        private String contentType;
        private Long tamanho;
    }
}
