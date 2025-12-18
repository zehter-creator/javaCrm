package com.ticari.repository;

import com.ticari.entity.Atik;
import com.ticari.entity.Urun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AtikRepository extends JpaRepository<Atik, Integer> {
    List<Atik> findByUrun(Urun urun);
    List<Atik> findByTarihBetween(LocalDateTime baslangic, LocalDateTime bitis);
}
