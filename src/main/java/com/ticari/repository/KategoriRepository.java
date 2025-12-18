package com.ticari.repository;

import com.ticari.entity.Kategori;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KategoriRepository extends JpaRepository<Kategori, Integer> {
    Optional<Kategori> findByKategoriAd(String kategoriAd);
}
