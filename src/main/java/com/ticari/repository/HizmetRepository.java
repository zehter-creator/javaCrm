package com.ticari.repository;

import com.ticari.entity.Hizmet;
import com.ticari.enums.HizmetTuru;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HizmetRepository extends JpaRepository<Hizmet, Integer> {
    List<Hizmet> findByTur(HizmetTuru tur);
    List<Hizmet> findByHizmetAdContainingIgnoreCase(String hizmetAd);
}
