package com.ticari.entity;

import com.ticari.enums.HesapTuru;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "KasaBanka")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KasaBanka {
    
    @Id
    @Column(name = "HesapID")
    private Integer hesapId;
    
    @Column(name = "HesapAdi", length = 50)
    private String hesapAdi;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "Tur", length = 10)
    private HesapTuru tur;
    
    @Column(name = "Bakiye", precision = 15, scale = 2)
    private BigDecimal bakiye = BigDecimal.ZERO;
    
    @OneToMany(mappedBy = "hesap", cascade = CascadeType.ALL)
    private List<FinansHareketi> finansHareketleri;
}
