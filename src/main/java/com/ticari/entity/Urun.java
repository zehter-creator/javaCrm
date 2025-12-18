package com.ticari.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "Urunler")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Urun {
    
    @Id
    @Column(name = "UrunID")
    private Integer urunId;
    
    @Column(name = "UrunAd", length = 100, nullable = false)
    private String urunAd;
    
    @ManyToOne
    @JoinColumn(name = "KategoriID", nullable = false)
    private Kategori kategori;
    
    @Column(name = "MevcutSatisFiyati", precision = 10, scale = 2)
    private BigDecimal mevcutSatisFiyati = BigDecimal.ZERO;
    
    @Column(name = "MinimumStokSeviyesi")
    private Integer minimumStokSeviyesi = 10;
    
    @Column(name = "MevcutStokMiktari")
    private Integer mevcutStokMiktari = 0;
    
    @OneToMany(mappedBy = "urun", cascade = CascadeType.ALL)
    private List<TeklifDetay> teklifDetaylari;
    
    @OneToMany(mappedBy = "urun", cascade = CascadeType.ALL)
    private List<StokGirisCikis> stokHareketleri;
    
    @OneToMany(mappedBy = "urun", cascade = CascadeType.ALL)
    private List<Atik> atiklar;
}
