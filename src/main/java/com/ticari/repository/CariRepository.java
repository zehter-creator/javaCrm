package com.ticari.repository;

import com.ticari.entity.Cari;
import com.ticari.enums.CariTuru;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CariRepository extends JpaRepository<Cari, Integer> {
    Optional<Cari> findByCariKod(String cariKod);
    List<Cari> findByTur(CariTuru tur);
    List<Cari> findByUnvanContainingIgnoreCase(String unvan);
    Optional<Cari> findByVergiNo(String vergiNo);
}
