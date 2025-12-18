package com.ticari.repository;

import com.ticari.entity.Personel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonelRepository extends JpaRepository<Personel, Integer> {
    List<Personel> findByAktifMi(Boolean aktifMi);
    List<Personel> findByGorev(String gorev);
    List<Personel> findByAdSoyadContainingIgnoreCase(String adSoyad);
}
