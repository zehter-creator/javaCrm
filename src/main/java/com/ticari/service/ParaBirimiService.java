package com.ticari.service;

import com.ticari.entity.ParaBirimi;
import com.ticari.repository.ParaBirimiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ParaBirimiService {
    
    private final ParaBirimiRepository paraBirimiRepository;
    
    public ParaBirimi kaydet(ParaBirimi paraBirimi) {
        return paraBirimiRepository.save(paraBirimi);
    }
    
    public Optional<ParaBirimi> getir(String paraKod) {
        return paraBirimiRepository.findById(paraKod);
    }
    
    public List<ParaBirimi> tumunuGetir() {
        return paraBirimiRepository.findAll();
    }
    
    public void sil(String paraKod) {
        paraBirimiRepository.deleteById(paraKod);
    }
    
    // English aliases for controllers
    public List<ParaBirimi> findAll() {
        return tumunuGetir();
    }
    
    public ParaBirimi save(ParaBirimi paraBirimi) {
        return kaydet(paraBirimi);
    }
    
    public Optional<ParaBirimi> findById(String paraKod) {
        return getir(paraKod);
    }
}
