package com.ticari.entity;

import com.ticari.enums.CariTuru;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "Cariler")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cari {
    
    @Id
    @Column(name = "CariID")
    private Integer cariId;
    
    @Column(name = "CariKod", length = 20, unique = true)
    private String cariKod;
    
    @Column(name = "Unvan", length = 200, nullable = false)
    private String unvan;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "Tur", length = 10)
    private CariTuru tur;
    
    @Column(name = "VergiNo", length = 20)
    private String vergiNo;
    
    @Column(name = "GuncelBakiye", precision = 15, scale = 2)
    private BigDecimal guncelBakiye = BigDecimal.ZERO;
    
    @OneToMany(mappedBy = "cari", cascade = CascadeType.ALL)
    private List<Teklif> teklifler;
    
    @OneToMany(mappedBy = "cari", cascade = CascadeType.ALL)
    private List<Siparis> siparisler;
    
    @OneToMany(mappedBy = "cari", cascade = CascadeType.ALL)
    private List<Fatura> faturalar;
    
    @OneToMany(mappedBy = "cari", cascade = CascadeType.ALL)
    private List<CekSenet> cekSenetler;
    
    @OneToMany(mappedBy = "cari", cascade = CascadeType.ALL)
    private List<FinansHareketi> finansHareketleri;
}
