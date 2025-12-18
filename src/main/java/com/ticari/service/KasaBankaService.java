package com.ticari.service;

import com.ticari.entity.KasaBanka;
import com.ticari.enums.HesapTuru;
import com.ticari.repository.KasaBankaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class KasaBankaService {
    
    private final KasaBankaRepository kasaBankaRepository;
    
    public KasaBanka kaydet(KasaBanka hesap) {
        return kasaBankaRepository.save(hesap);
    }
    
    public Optional<KasaBanka> getir(Integer id) {
        return kasaBankaRepository.findById(id);
    }
    
    public List<KasaBanka> tumunuGetir() {
        return kasaBankaRepository.findAll();
    }
    
    public void sil(Integer id) {
        kasaBankaRepository.deleteById(id);
    }
    
    public List<KasaBanka> tureBul(HesapTuru tur) {
        return kasaBankaRepository.findByTur(tur);
    }
    
    public void bakiyeGuncelle(Integer hesapId, BigDecimal tutar) {
        Optional<KasaBanka> hesapOpt = kasaBankaRepository.findById(hesapId);
        if (hesapOpt.isPresent()) {
            KasaBanka hesap = hesapOpt.get();
            hesap.setBakiye(hesap.getBakiye().add(tutar));
            kasaBankaRepository.save(hesap);
        }
    }
    
    public BigDecimal toplamBakiyeHesapla() {
        return kasaBankaRepository.findAll().stream()
                .map(KasaBanka::getBakiye)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    // English aliases for controllers
    public List<KasaBanka> findAll() {
        return tumunuGetir();
    }
    
    public KasaBanka save(KasaBanka hesap) {
        return kaydet(hesap);
    }
    
    public Optional<KasaBanka> findById(Integer id) {
        return getir(id);
    }
    
    public BigDecimal getToplamBakiye() {
        return toplamBakiyeHesapla();
    }
}
