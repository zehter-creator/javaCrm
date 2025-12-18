package com.ticari.service;

import com.ticari.entity.Cari;
import com.ticari.enums.CariTuru;
import com.ticari.repository.CariRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CariService {
    
    private final CariRepository cariRepository;
    
    public Cari kaydet(Cari cari) {
        return cariRepository.save(cari);
    }
    
    public Optional<Cari> getir(Integer id) {
        return cariRepository.findById(id);
    }
    
    public List<Cari> tumunuGetir() {
        return cariRepository.findAll();
    }
    
    public void sil(Integer id) {
        cariRepository.deleteById(id);
    }
    
    public List<Cari> tureBul(CariTuru tur) {
        return cariRepository.findByTur(tur);
    }
    
    public List<Cari> unvanaGoreAra(String unvan) {
        return cariRepository.findByUnvanContainingIgnoreCase(unvan);
    }
    
    public Optional<Cari> cariKodaBul(String cariKod) {
        return cariRepository.findByCariKod(cariKod);
    }
    
    public void bakiyeGuncelle(Integer cariId, BigDecimal tutar) {
        Optional<Cari> cariOpt = cariRepository.findById(cariId);
        if (cariOpt.isPresent()) {
            Cari cari = cariOpt.get();
            cari.setGuncelBakiye(cari.getGuncelBakiye().add(tutar));
            cariRepository.save(cari);
        }
    }
    
    // English aliases for controllers
    public List<Cari> findAll() {
        return tumunuGetir();
    }
    
    public Cari save(Cari cari) {
        return kaydet(cari);
    }
    
    public Optional<Cari> findById(Integer id) {
        return getir(id);
    }
}
