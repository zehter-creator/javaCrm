package com.ticari.entity;

import com.ticari.enums.CekSenetDurumu;
import com.ticari.enums.CekSenetTuru;
import com.ticari.enums.CekSenetYonu;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "CekSenetler")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CekSenet {
    
    @Id
    @Column(name = "EvrakID")
    private Integer evrakId;
    
    @ManyToOne
    @JoinColumn(name = "CariID")
    private Cari cari;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "Tur", length = 10)
    private CekSenetTuru tur;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "Yon", length = 10)
    private CekSenetYonu yon;
    
    @Column(name = "VadeTarihi", nullable = false)
    private LocalDate vadeTarihi;
    
    @Column(name = "Tutar", precision = 15, scale = 2, nullable = false)
    private BigDecimal tutar;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "Durum", length = 20)
    private CekSenetDurumu durum = CekSenetDurumu.PORTFOYDE;
    
    @Column(name = "BankaAdi", length = 50)
    private String bankaAdi;
    
    @OneToMany(mappedBy = "evrak", cascade = CascadeType.ALL)
    private List<FinansHareketi> finansHareketleri;
}
