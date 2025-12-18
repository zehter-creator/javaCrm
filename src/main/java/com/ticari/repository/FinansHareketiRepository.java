package com.ticari.repository;

import com.ticari.entity.Cari;
import com.ticari.entity.FinansHareketi;
import com.ticari.entity.KasaBanka;
import com.ticari.enums.FinansIslemTuru;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FinansHareketiRepository extends JpaRepository<FinansHareketi, Integer> {
    List<FinansHareketi> findByHesap(KasaBanka hesap);
    List<FinansHareketi> findByCari(Cari cari);
    List<FinansHareketi> findByIslemTuru(FinansIslemTuru islemTuru);
    List<FinansHareketi> findByTarihBetween(LocalDateTime baslangic, LocalDateTime bitis);
}
