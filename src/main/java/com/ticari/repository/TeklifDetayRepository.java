package com.ticari.repository;

import com.ticari.entity.Teklif;
import com.ticari.entity.TeklifDetay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeklifDetayRepository extends JpaRepository<TeklifDetay, Integer> {
    List<TeklifDetay> findByTeklif(Teklif teklif);
}
