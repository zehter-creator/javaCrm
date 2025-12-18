package com.ticari.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "FaturaHizmetKalemleri")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FaturaHizmetKalemi {
    
    @Id
    @Column(name = "KalemID")
    private Integer kalemId;
    
    @ManyToOne
    @JoinColumn(name = "FaturaID", nullable = false)
    private Fatura fatura;
    
    @ManyToOne
    @JoinColumn(name = "HizmetID", nullable = false)
    private Hizmet hizmet;
    
    @Column(name = "Aciklama", length = 100)
    private String aciklama;
    
    @Column(name = "Tutar", precision = 10, scale = 2)
    private BigDecimal tutar;
    
    @ManyToOne
    @JoinColumn(name = "ParaBirimi")
    private ParaBirimi paraBirimi;
    
    @Column(name = "Kur", precision = 18, scale = 6)
    private BigDecimal kur = BigDecimal.ONE;
}
