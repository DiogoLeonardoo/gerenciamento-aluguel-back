package com.inhouse.project.service;

import com.inhouse.project.domain.Casa;
import com.inhouse.project.domain.FotoCasa;
import com.inhouse.project.exceptions.ResourceNotFoundException;
import com.inhouse.project.repository.CasaRepository;
import com.inhouse.project.repository.FotoCasaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FotoCasaService {

    private final FotoCasaRepository fotoCasaRepository;
    private final CasaRepository casaRepository;

    /**
     * Salva uma nova foto para uma casa específica
     * 
     * @param casaId ID da casa
     * @param file Arquivo de imagem
     * @return A entidade FotoCasa salva
     * @throws IOException Se ocorrer erro ao processar o arquivo
     */
    @Transactional
    public FotoCasa salvarFoto(Long casaId, MultipartFile file) throws IOException {
        Casa casa = casaRepository.findById(casaId)
                .orElseThrow(() -> new ResourceNotFoundException("Casa não encontrada com o ID: " + casaId));
        
        FotoCasa foto = new FotoCasa();
        foto.setCasa(casa);
        foto.setNomeArquivo(file.getOriginalFilename());
        foto.setDescricao(file.getOriginalFilename()); // Podemos melhorar isso mais tarde
        foto.setContentType(file.getContentType());
        foto.setTamanho(file.getSize());
        foto.setConteudoArquivo(file.getBytes());
        
        return fotoCasaRepository.save(foto);
    }
    
    /**
     * Recupera uma foto pelo ID
     * 
     * @param id ID da foto
     * @return A entidade FotoCasa
     */
    @Transactional(readOnly = true)
    public FotoCasa getFotoById(Long id) {
        return fotoCasaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Foto não encontrada com o ID: " + id));
    }
    
    /**
     * Lista todas as fotos de uma casa
     * 
     * @param casaId ID da casa
     * @return Lista de entidades FotoCasa
     */
    @Transactional(readOnly = true)
    public List<FotoCasa> getFotosByCasaId(Long casaId) {
        Casa casa = casaRepository.findById(casaId)
                .orElseThrow(() -> new ResourceNotFoundException("Casa não encontrada com o ID: " + casaId));
                
        return fotoCasaRepository.findByCasa(casa);
    }
    
    /**
     * Remove uma foto pelo ID
     * 
     * @param id ID da foto
     */
    @Transactional
    public void deleteFoto(Long id) {
        if (!fotoCasaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Foto não encontrada com o ID: " + id);
        }
        fotoCasaRepository.deleteById(id);
    }
}
