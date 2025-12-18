package com.ticari.entity;

import com.ticari.enums.StokIslemTuru;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "StokGirisCikis")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StokGirisCikis {
    
    @Id
    @Column(name = "HareketID")
    private Integer hareketId;
    
    @ManyToOne
    @JoinColumn(name = "FaturaID", nullable = false)
    private Fatura fatura;
    
    @ManyToOne
    @JoinColumn(name = "UrunID", nullable = false)
    private Urun urun;
    
    @ManyToOne
    @JoinColumn(name = "SiparisID")
    private Siparis siparis;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "IslemTuru", length = 10, nullable = false)
    private StokIslemTuru islemTuru;
    
    @Column(name = "Miktar", nullable = false)
    private Integer miktar;
    
    @Column(name = "BirimFiyat", precision = 10, scale = 2, nullable = false)
    private BigDecimal birimFiyat;
    
    @Column(name = "Tarih")
    private LocalDateTime tarih = LocalDateTime.now();
    
    @ManyToOne
    @JoinColumn(name = "ParaBirimi")
    private ParaBirimi paraBirimi;
    
    @Column(name = "Kur", precision = 18, scale = 6)
    private BigDecimal kur = BigDecimal.ONE;
}
