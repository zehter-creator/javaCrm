package com.ticari.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "Kategoriler")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Kategori {
    
    @Id
    @Column(name = "KategoriID")
    private Integer kategoriId;
    
    @Column(name = "KategoriAd", length = 50, unique = true, nullable = false)
    private String kategoriAd;
    
    @OneToMany(mappedBy = "kategori", cascade = CascadeType.ALL)
    private List<Urun> urunler;
}
