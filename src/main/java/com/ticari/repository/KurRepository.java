package com.ticari.repository;

import com.ticari.entity.Kur;
import com.ticari.entity.ParaBirimi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface KurRepository extends JpaRepository<Kur, Integer> {
    List<Kur> findByParaBirimi(ParaBirimi paraBirimi);
    Optional<Kur> findByParaBirimiAndKurTarihi(ParaBirimi paraBirimi, LocalDate kurTarihi);
    List<Kur> findByKurTarihiBetween(LocalDate baslangic, LocalDate bitis);
}
