package com.ticari.service;

import com.ticari.entity.Fatura;
import com.ticari.entity.StokGirisCikis;
import com.ticari.enums.FaturaTuru;
import com.ticari.enums.StokIslemTuru;
import com.ticari.repository.FaturaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class FaturaService {
    
    private final FaturaRepository faturaRepository;
    private final CariService cariService;
    private final UrunService urunService;
    
    public Fatura kaydet(Fatura fatura) {
        Fatura kaydedilen = faturaRepository.save(fatura);
        
        // Fatura kaydedildiğinde stok ve cari bakiye güncellemeleri
        if (kaydedilen.getStokGirisCikislar() != null) {
            for (StokGirisCikis hareket : kaydedilen.getStokGirisCikislar()) {
                if (hareket.getIslemTuru() == StokIslemTuru.GIRIS) {
                    urunService.stokGuncelle(hareket.getUrun().getUrunId(), hareket.getMiktar());
                } else if (hareket.getIslemTuru() == StokIslemTuru.CIKIS) {
                    urunService.stokGuncelle(hareket.getUrun().getUrunId(), -hareket.getMiktar());
                }
            }
        }
        
        // Cari bakiye güncelle
        if (kaydedilen.getTur() == FaturaTuru.SATIS) {
            cariService.bakiyeGuncelle(kaydedilen.getCari().getCariId(), kaydedilen.getGenelToplam());
        } else if (kaydedilen.getTur() == FaturaTuru.ALIS) {
            cariService.bakiyeGuncelle(kaydedilen.getCari().getCariId(), kaydedilen.getGenelToplam().negate());
        }
        
        return kaydedilen;
    }
    
    public Optional<Fatura> getir(Integer id) {
        return faturaRepository.findById(id);
    }
    
    public List<Fatura> tumunuGetir() {
        return faturaRepository.findAll();
    }
    
    public void sil(Integer id) {
        faturaRepository.deleteById(id);
    }
    
    public List<Fatura> tureBul(FaturaTuru tur) {
        return faturaRepository.findByTur(tur);
    }
    
    public Optional<Fatura> faturaNosunaGoreBul(String faturaNo) {
        return faturaRepository.findByFaturaNo(faturaNo);
    }
    
    public List<Fatura> tarihAraliginaGoreBul(LocalDateTime baslangic, LocalDateTime bitis) {
        return faturaRepository.findByFaturaTarihiBetween(baslangic, bitis);
    }
    
    public BigDecimal toplamSatisHesapla(LocalDateTime baslangic, LocalDateTime bitis) {
        List<Fatura> faturalar = faturaRepository.findByFaturaTarihiBetween(baslangic, bitis);
        return faturalar.stream()
                .filter(f -> f.getTur() == FaturaTuru.SATIS)
                .map(Fatura::getGenelToplam)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
