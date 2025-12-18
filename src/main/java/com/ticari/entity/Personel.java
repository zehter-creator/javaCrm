package com.ticari.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "Personeller")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Personel {
    
    @Id
    @Column(name = "PersonelID")
    private Integer personelId;
    
    @Column(name = "AdSoyad", length = 100, nullable = false)
    private String adSoyad;
    
    @Column(name = "Gorev", length = 50)
    private String gorev;
    
    @Column(name = "Maas", precision = 10, scale = 2)
    private BigDecimal maas;
    
    @Column(name = "IseGirisTarihi")
    private LocalDate iseGirisTarihi;
    
    @Column(name = "AktifMi")
    private Boolean aktifMi = true;
    
    @OneToMany(mappedBy = "personel", cascade = CascadeType.ALL)
    private List<FinansHareketi> finansHareketleri;
}
