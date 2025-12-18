package com.ticari.repository;

import com.ticari.entity.Cari;
import com.ticari.entity.Fatura;
import com.ticari.enums.FaturaTuru;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FaturaRepository extends JpaRepository<Fatura, Integer> {
    List<Fatura> findByCari(Cari cari);
    List<Fatura> findByTur(FaturaTuru tur);
    Optional<Fatura> findByFaturaNo(String faturaNo);
    List<Fatura> findByFaturaTarihiBetween(LocalDateTime baslangic, LocalDateTime bitis);
}
