package com.ticari.repository;

import com.ticari.entity.StokGirisCikis;
import com.ticari.entity.Urun;
import com.ticari.enums.StokIslemTuru;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StokGirisCikisRepository extends JpaRepository<StokGirisCikis, Integer> {
    List<StokGirisCikis> findByUrun(Urun urun);
    List<StokGirisCikis> findByIslemTuru(StokIslemTuru islemTuru);
    List<StokGirisCikis> findByTarihBetween(LocalDateTime baslangic, LocalDateTime bitis);
}
