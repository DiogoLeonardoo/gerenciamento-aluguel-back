package com.inhouse.project.resource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/info")
public class ApiInfoResource {

    @GetMapping
    public ResponseEntity<Map<String, String>> getApiInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("version", "1.0.0");
        info.put("title", "API de Controle de Reservas");
        info.put("description", "API para gerenciamento de casas, reservas e hóspedes");
        return ResponseEntity.ok(info);
    }
    
    @GetMapping("/casa-exemplo")
    public ResponseEntity<String> getCasaExemplo() {
        try {
            Resource resource = new ClassPathResource("static/casa-exemplo.json");
            byte[] bytes = resource.getInputStream().readAllBytes();
            String json = new String(bytes, StandardCharsets.UTF_8);
            return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(json);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("{ \"error\": \"Erro ao ler arquivo de exemplo\" }");
        }
    }
    
    @GetMapping("/tutorial-cadastro-casa")
    public ResponseEntity<Map<String, Object>> getCadastroTutorial() {
        Map<String, Object> tutorial = new HashMap<>();
        tutorial.put("title", "Tutorial de Cadastro de Casa");
        
        // Opção 1: Endpoint para teste automatizado
        Map<String, String> opcao1 = new HashMap<>();
        opcao1.put("description", "Endpoint de teste que cria uma casa com dados padrão");
        opcao1.put("url", "/api/casas/teste");
        opcao1.put("method", "POST");
        opcao1.put("authentication", "JWT Bearer Token (Usuário com role PROPRIETARIO)");
        
        // Opção 2: Endpoint JSON
        Map<String, Object> opcao2 = new HashMap<>();
        opcao2.put("description", "Endpoint JSON para criar casa (sem fotos)");
        opcao2.put("url", "/api/casas/json");
        opcao2.put("method", "POST");
        opcao2.put("contentType", "application/json");
        opcao2.put("authentication", "JWT Bearer Token (Usuário com role PROPRIETARIO)");
        opcao2.put("exemplo", "/api/info/casa-exemplo");
        
        // Opção 3: Endpoint Multipart
        Map<String, String> opcao3 = new HashMap<>();
        opcao3.put("description", "Endpoint Multipart para criar casa com fotos");
        opcao3.put("url", "/api/casas");
        opcao3.put("method", "POST");
        opcao3.put("contentType", "multipart/form-data");
        opcao3.put("authentication", "JWT Bearer Token (Usuário com role PROPRIETARIO)");
        opcao3.put("nota", "Ao usar este endpoint no Swagger, preencha cada campo individualmente");
        
        // Opção 4: Endpoint Form
        Map<String, String> opcao4 = new HashMap<>();
        opcao4.put("description", "Endpoint de formulário para criar casa usando @ModelAttribute");
        opcao4.put("url", "/api/casas/form");
        opcao4.put("method", "POST");
        opcao4.put("contentType", "multipart/form-data");
        opcao4.put("authentication", "JWT Bearer Token (Usuário com role PROPRIETARIO)");
        
        Map<String, Object> opcoes = new HashMap<>();
        opcoes.put("opção1", opcao1);
        opcoes.put("opção2", opcao2);
        opcoes.put("opção3", opcao3);
        opcoes.put("opção4", opcao4);
        
        tutorial.put("opções", opcoes);
        
        return ResponseEntity.ok(tutorial);
    }
}
