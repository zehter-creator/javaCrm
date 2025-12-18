package com.ticari.entity;

import com.ticari.enums.SiparisDurumu;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Siparisler")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Siparis {
    
    @Id
    @Column(name = "SiparisID")
    private Integer siparisId;
    
    @ManyToOne
    @JoinColumn(name = "TeklifID")
    private Teklif teklif;
    
    @ManyToOne
    @JoinColumn(name = "CariID", nullable = false)
    private Cari cari;
    
    @Column(name = "SiparisTarihi")
    private LocalDateTime siparisTarihi = LocalDateTime.now();
    
    @Enumerated(EnumType.STRING)
    @Column(name = "Durum", length = 20)
    private SiparisDurumu durum = SiparisDurumu.HAZIRLANIYOR;
    
    @OneToMany(mappedBy = "siparis", cascade = CascadeType.ALL)
    private List<StokGirisCikis> stokHareketleri;
}
