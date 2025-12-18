package com.ticari.service;

import com.ticari.entity.StokGirisCikis;
import com.ticari.entity.Urun;
import com.ticari.enums.StokIslemTuru;
import com.ticari.repository.StokGirisCikisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class StokService {
    
    private final StokGirisCikisRepository stokRepository;
    
    public StokGirisCikis kaydet(StokGirisCikis stokHareket) {
        return stokRepository.save(stokHareket);
    }
    
    public Optional<StokGirisCikis> getir(Integer id) {
        return stokRepository.findById(id);
    }
    
    public List<StokGirisCikis> tumunuGetir() {
        return stokRepository.findAll();
    }
    
    public void sil(Integer id) {
        stokRepository.deleteById(id);
    }
    
    public List<StokGirisCikis> uruneBul(Urun urun) {
        return stokRepository.findByUrun(urun);
    }
    
    public List<StokGirisCikis> islemTuruneBul(StokIslemTuru islemTuru) {
        return stokRepository.findByIslemTuru(islemTuru);
    }
    
    public List<StokGirisCikis> tarihAraliginaGoreBul(LocalDateTime baslangic, LocalDateTime bitis) {
        return stokRepository.findByTarihBetween(baslangic, bitis);
    }
    
    // English aliases for controllers
    public StokGirisCikis save(StokGirisCikis stokHareket) {
        return kaydet(stokHareket);
    }
    
    public List<StokGirisCikis> findAll() {
        return tumunuGetir();
    }
    
    public Optional<StokGirisCikis> findById(Integer id) {
        return getir(id);
    }
}
