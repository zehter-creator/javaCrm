package com.ticari.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "Kurlar")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Kur {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "KurID")
    private Integer kurId;
    
    @ManyToOne
    @JoinColumn(name = "ParaKod")
    private ParaBirimi paraBirimi;
    
    @Column(name = "KurTarihi", nullable = false)
    private LocalDate kurTarihi;
    
    @Column(name = "Kur", precision = 18, scale = 6, nullable = false)
    private BigDecimal kur;
    
    @Column(name = "Kaynak", length = 20)
    private String kaynak = "TCMB";
}
