package com.ticari.entity;

import com.ticari.enums.FinansIslemTuru;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "FinansHareketleri")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinansHareketi {
    
    @Id
    @Column(name = "HareketID")
    private Integer hareketId;
    
    @ManyToOne
    @JoinColumn(name = "HesapID")
    private KasaBanka hesap;
    
    @ManyToOne
    @JoinColumn(name = "CariID")
    private Cari cari;
    
    @ManyToOne
    @JoinColumn(name = "PersonelID")
    private Personel personel;
    
    @ManyToOne
    @JoinColumn(name = "EvrakID")
    private CekSenet evrak;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "IslemTuru", length = 20)
    private FinansIslemTuru islemTuru;
    
    @Column(name = "Tutar", precision = 15, scale = 2)
    private BigDecimal tutar;
    
    @Column(name = "Tarih")
    private LocalDateTime tarih = LocalDateTime.now();
    
    @ManyToOne
    @JoinColumn(name = "ParaBirimi")
    private ParaBirimi paraBirimi;
    
    @Column(name = "Kur", precision = 18, scale = 6)
    private BigDecimal kur = BigDecimal.ONE;
}
