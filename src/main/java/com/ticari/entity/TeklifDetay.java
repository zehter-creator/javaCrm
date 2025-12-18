package com.ticari.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "TeklifDetaylari")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeklifDetay {
    
    @Id
    @Column(name = "DetayID")
    private Integer detayId;
    
    @ManyToOne
    @JoinColumn(name = "TeklifID", nullable = false)
    private Teklif teklif;
    
    @ManyToOne
    @JoinColumn(name = "UrunID")
    private Urun urun;
    
    @Column(name = "Miktar")
    private Integer miktar;
    
    @Column(name = "BirimFiyat", precision = 10, scale = 2)
    private BigDecimal birimFiyat;
}
