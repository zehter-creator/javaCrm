package com.ticari.repository;

import com.ticari.entity.Cari;
import com.ticari.entity.Teklif;
import com.ticari.enums.TeklifDurumu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TeklifRepository extends JpaRepository<Teklif, Integer> {
    List<Teklif> findByCari(Cari cari);
    List<Teklif> findByDurum(TeklifDurumu durum);
    List<Teklif> findByTeklifTarihiBetween(LocalDateTime baslangic, LocalDateTime bitis);
}
