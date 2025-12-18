package com.ticari.service;

import com.ticari.entity.FinansHareketi;
import com.ticari.enums.FinansIslemTuru;
import com.ticari.repository.FinansHareketiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class FinansService {
    
    private final FinansHareketiRepository finansRepository;
    private final KasaBankaService kasaBankaService;
    private final CariService cariService;
    
    public FinansHareketi kaydet(FinansHareketi hareket) {
        FinansHareketi kaydedilen = finansRepository.save(hareket);
        
        // Kasa/Banka bakiyesini güncelle
        if (kaydedilen.getHesap() != null) {
            if (kaydedilen.getIslemTuru() == FinansIslemTuru.TAHSILAT) {
                kasaBankaService.bakiyeGuncelle(kaydedilen.getHesap().getHesapId(), kaydedilen.getTutar());
            } else if (kaydedilen.getIslemTuru() == FinansIslemTuru.ODEME || 
                       kaydedilen.getIslemTuru() == FinansIslemTuru.MAAS) {
                kasaBankaService.bakiyeGuncelle(kaydedilen.getHesap().getHesapId(), kaydedilen.getTutar().negate());
            }
        }
        
        // Cari bakiyesini güncelle
        if (kaydedilen.getCari() != null) {
            if (kaydedilen.getIslemTuru() == FinansIslemTuru.TAHSILAT) {
                cariService.bakiyeGuncelle(kaydedilen.getCari().getCariId(), kaydedilen.getTutar().negate());
            } else if (kaydedilen.getIslemTuru() == FinansIslemTuru.ODEME) {
                cariService.bakiyeGuncelle(kaydedilen.getCari().getCariId(), kaydedilen.getTutar());
            }
        }
        
        return kaydedilen;
    }
    
    public Optional<FinansHareketi> getir(Integer id) {
        return finansRepository.findById(id);
    }
    
    public List<FinansHareketi> tumunuGetir() {
        return finansRepository.findAll();
    }
    
    public void sil(Integer id) {
        finansRepository.deleteById(id);
    }
    
    public List<FinansHareketi> islemTuruneBul(FinansIslemTuru islemTuru) {
        return finansRepository.findByIslemTuru(islemTuru);
    }
    
    public List<FinansHareketi> tarihAraliginaGoreBul(LocalDateTime baslangic, LocalDateTime bitis) {
        return finansRepository.findByTarihBetween(baslangic, bitis);
    }
}
