package com.ticari.repository;

import com.ticari.entity.CekSenet;
import com.ticari.enums.CekSenetDurumu;
import com.ticari.enums.CekSenetTuru;
import com.ticari.enums.CekSenetYonu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CekSenetRepository extends JpaRepository<CekSenet, Integer> {
    List<CekSenet> findByTur(CekSenetTuru tur);
    List<CekSenet> findByYon(CekSenetYonu yon);
    List<CekSenet> findByDurum(CekSenetDurumu durum);
    List<CekSenet> findByVadeTarihiBetween(LocalDate baslangic, LocalDate bitis);
}
