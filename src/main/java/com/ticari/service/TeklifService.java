package com.ticari.service;

import com.ticari.entity.Cari;
import com.ticari.entity.Teklif;
import com.ticari.enums.TeklifDurumu;
import com.ticari.repository.TeklifRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class TeklifService {
    
    private final TeklifRepository teklifRepository;
    
    public Teklif kaydet(Teklif teklif) {
        return teklifRepository.save(teklif);
    }
    
    public Optional<Teklif> getir(Integer id) {
        return teklifRepository.findById(id);
    }
    
    public List<Teklif> tumunuGetir() {
        return teklifRepository.findAll();
    }
    
    public void sil(Integer id) {
        teklifRepository.deleteById(id);
    }
    
    public List<Teklif> cariyeBul(Cari cari) {
        return teklifRepository.findByCari(cari);
    }
    
    public List<Teklif> durumaBul(TeklifDurumu durum) {
        return teklifRepository.findByDurum(durum);
    }
    
    public List<Teklif> tarihAraliginaGoreBul(LocalDateTime baslangic, LocalDateTime bitis) {
        return teklifRepository.findByTeklifTarihiBetween(baslangic, bitis);
    }
    
    public void durumGuncelle(Integer teklifId, TeklifDurumu durum) {
        Optional<Teklif> teklifOpt = teklifRepository.findById(teklifId);
        if (teklifOpt.isPresent()) {
            Teklif teklif = teklifOpt.get();
            teklif.setDurum(durum);
            teklifRepository.save(teklif);
        }
    }
}
