package com.ticari.repository;

import com.ticari.entity.Kategori;
import com.ticari.entity.Urun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UrunRepository extends JpaRepository<Urun, Integer> {
    List<Urun> findByKategori(Kategori kategori);
    List<Urun> findByUrunAdContainingIgnoreCase(String urunAd);
    
    @Query("SELECT u FROM Urun u WHERE u.mevcutStokMiktari < u.minimumStokSeviyesi")
    List<Urun> findDusukStokluUrunler();
}
