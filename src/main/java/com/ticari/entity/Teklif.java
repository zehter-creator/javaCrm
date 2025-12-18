package com.ticari.entity;

import com.ticari.enums.TeklifDurumu;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Teklifler")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Teklif {
    
    @Id
    @Column(name = "TeklifID")
    private Integer teklifId;
    
    @ManyToOne
    @JoinColumn(name = "CariID", nullable = false)
    private Cari cari;
    
    @Column(name = "TeklifTarihi")
    private LocalDateTime teklifTarihi = LocalDateTime.now();
    
    @Column(name = "GecerlilikTarihi")
    private LocalDate gecerlilikTarihi;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "Durum", length = 20)
    private TeklifDurumu durum = TeklifDurumu.BEKLIYOR;
    
    @Column(name = "ToplamTutar", precision = 15, scale = 2)
    private BigDecimal toplamTutar;
    
    @OneToMany(mappedBy = "teklif", cascade = CascadeType.ALL)
    private List<TeklifDetay> teklifDetaylari;
    
    @OneToMany(mappedBy = "teklif", cascade = CascadeType.ALL)
    private List<Siparis> siparisler;
}
