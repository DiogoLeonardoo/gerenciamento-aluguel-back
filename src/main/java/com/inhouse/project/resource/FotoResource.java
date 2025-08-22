package com.inhouse.project.resource;

import com.inhouse.project.domain.FotoCasa;
import com.inhouse.project.exceptions.BusinessException;
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
import java.util.ArrayList;
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
    
    @PostMapping("/upload/multiplas/casa/{casaId}")
    @Operation(summary = "Faz upload de múltiplas fotos (até 10) para uma casa específica")
    public ResponseEntity<Map<String, Object>> uploadMultiplasFotos(
            @PathVariable Long casaId,
            @RequestParam("files") MultipartFile[] files) throws IOException {
        
        // Validar o número máximo de arquivos na requisição
        if (files.length > 10) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("erro", "O número máximo de fotos por upload é 10");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        // Verificar se já existe o limite máximo de fotos para esta casa
        try {
            // Obter o número atual de fotos
            List<FotoCasa> fotosExistentes = fotoCasaService.getFotosByCasaId(casaId);
            int fotosAtuais = fotosExistentes.size();
            int fotosRestantes = 10 - fotosAtuais;
            
            if (fotosRestantes <= 0) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("erro", "Esta casa já atingiu o limite máximo de 10 fotos");
                errorResponse.put("totalFotos", fotosAtuais);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Se o número de arquivos enviados excede o limite restante
            if (files.length > fotosRestantes) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("aviso", "Apenas " + fotosRestantes + " fotos serão processadas devido ao limite máximo de 10 fotos por casa");
                errorResponse.put("fotosAtuais", fotosAtuais);
                errorResponse.put("fotosRestantes", fotosRestantes);
                log.warn("Casa ID {} já possui {} fotos. Apenas {} serão processadas neste upload.", 
                         casaId, fotosAtuais, fotosRestantes);
            }
            
            List<Map<String, Object>> fotosResponse = new ArrayList<>();
            int fotosProcessadas = 0;
            
            // Processar cada arquivo, até o limite restante
            for (MultipartFile file : files) {
                if (fotosProcessadas >= fotosRestantes) {
                    break;
                }
                
                if (!file.isEmpty()) {
                    try {
                        FotoCasa foto = fotoCasaService.salvarFoto(casaId, file);
                        
                        Map<String, Object> fotoInfo = new HashMap<>();
                        fotoInfo.put("id", foto.getId());
                        fotoInfo.put("nome", foto.getNomeArquivo());
                        fotoInfo.put("url", "/api/fotos/" + foto.getId());
                        
                        fotosResponse.add(fotoInfo);
                        fotosProcessadas++;
                    } catch (IOException e) {
                        log.error("Erro ao fazer upload do arquivo: " + file.getOriginalFilename(), e);
                    } catch (BusinessException e) {
                        // Se o limite foi atingido durante o processamento
                        log.warn("Limite atingido durante o processamento: {}", e.getMessage());
                        break;
                    }
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalProcessado", fotosResponse.size());
            response.put("fotos", fotosResponse);
            response.put("totalAtual", fotosAtuais + fotosResponse.size());
            response.put("limiteMaximo", 10);
            response.put("fotosRestantes", 10 - (fotosAtuais + fotosResponse.size()));
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ResourceNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("erro", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            log.error("Erro ao processar fotos para casa ID {}: {}", casaId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("erro", "Erro ao processar fotos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
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
        headers.setContentDisposition(org.springframework.http.ContentDisposition
            .builder("inline")
            .filename(foto.getNomeArquivo())
            .build());
        
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
        headers.setContentDisposition(org.springframework.http.ContentDisposition
            .builder("inline")
            .filename(fotoPrincipal.getNomeArquivo())
            .build());
        headers.setCacheControl("max-age=86400"); // 1 dia de cache
        
        // Retornar a resposta
        return new ResponseEntity<>(fotoPrincipal.getConteudoArquivo(), headers, HttpStatus.OK);
    }
    
    /**
     * Lista todas as fotos disponíveis para uma casa específica
     */
    @GetMapping("/lista/casa/{casaId}")
    @Operation(summary = "Lista todas as fotos de uma casa")
    public ResponseEntity<?> listarFotosDaCasa(@PathVariable Long casaId) {
        try {
            // Buscar todas as fotos da casa
            List<FotoCasa> fotos = fotoCasaService.getFotosByCasaId(casaId);
            
            if (fotos.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("mensagem", "Esta casa não possui fotos cadastradas");
                response.put("casaId", casaId);
                return ResponseEntity.ok(response);
            }
            
            // Identificar a foto principal
            FotoCasa fotoPrincipal = fotos.stream()
                .filter(f -> Boolean.TRUE.equals(f.getPrincipal()))
                .findFirst()
                .orElse(fotos.get(0));
            
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
            
            // Criar resposta estruturada
            Map<String, Object> response = new HashMap<>();
            response.put("casaId", casaId);
            response.put("totalFotos", fotos.size());
            response.put("fotoPrincipalId", fotoPrincipal.getId());
            response.put("fotoPrincipalUrl", "/api/fotos/" + fotoPrincipal.getId());
            response.put("fotos", fotosInfo);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erro ao buscar fotos da casa: " + casaId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("erro", "Erro ao buscar fotos da casa");
            errorResponse.put("mensagem", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Remove uma foto pelo ID")
    public ResponseEntity<Void> deleteFoto(@PathVariable Long id) {
        fotoCasaService.deleteFoto(id);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{id}/principal")
    @Operation(summary = "Define uma foto como a principal da casa")
    public ResponseEntity<Map<String, Object>> marcarFotoComoPrincipal(@PathVariable Long id) {
        FotoCasa foto = fotoCasaService.marcarComoPrincipal(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensagem", "Foto definida como principal com sucesso");
        response.put("id", foto.getId());
        response.put("casaId", foto.getCasa().getId());
        
        return ResponseEntity.ok(response);
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
