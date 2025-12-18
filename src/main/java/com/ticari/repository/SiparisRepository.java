package com.ticari.repository;

import com.ticari.entity.Cari;
import com.ticari.entity.Siparis;
import com.ticari.enums.SiparisDurumu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SiparisRepository extends JpaRepository<Siparis, Integer> {
    List<Siparis> findByCari(Cari cari);
    List<Siparis> findByDurum(SiparisDurumu durum);
    List<Siparis> findBySiparisTarihiBetween(LocalDateTime baslangic, LocalDateTime bitis);
}
