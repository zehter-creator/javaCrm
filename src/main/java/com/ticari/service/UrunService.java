package com.ticari.service;

import com.ticari.entity.Kategori;
import com.ticari.entity.Urun;
import com.ticari.repository.UrunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UrunService {
    
    private final UrunRepository urunRepository;
    
    public Urun kaydet(Urun urun) {
        return urunRepository.save(urun);
    }
    
    public Optional<Urun> getir(Integer id) {
        return urunRepository.findById(id);
    }
    
    public List<Urun> tumunuGetir() {
        return urunRepository.findAll();
    }
    
    public void sil(Integer id) {
        urunRepository.deleteById(id);
    }
    
    public List<Urun> kategoriyeGoreGetir(Kategori kategori) {
        return urunRepository.findByKategori(kategori);
    }
    
    public List<Urun> urunAdaGoreAra(String urunAd) {
        return urunRepository.findByUrunAdContainingIgnoreCase(urunAd);
    }
    
    public List<Urun> dusukStokluUrunleriGetir() {
        return urunRepository.findDusukStokluUrunler();
    }
    
    public void stokGuncelle(Integer urunId, Integer miktar) {
        Optional<Urun> urunOpt = urunRepository.findById(urunId);
        if (urunOpt.isPresent()) {
            Urun urun = urunOpt.get();
            urun.setMevcutStokMiktari(urun.getMevcutStokMiktari() + miktar);
            urunRepository.save(urun);
        }
    }
}
