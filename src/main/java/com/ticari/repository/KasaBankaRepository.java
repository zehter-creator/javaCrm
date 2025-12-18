package com.ticari.repository;

import com.ticari.entity.KasaBanka;
import com.ticari.enums.HesapTuru;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KasaBankaRepository extends JpaRepository<KasaBanka, Integer> {
    List<KasaBanka> findByTur(HesapTuru tur);
    List<KasaBanka> findByHesapAdiContainingIgnoreCase(String hesapAdi);
}
