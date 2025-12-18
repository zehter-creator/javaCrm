package com.ticari.entity;

import com.ticari.enums.FaturaTuru;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Faturalar")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Fatura {
    
    @Id
    @Column(name = "FaturaID")
    private Integer faturaId;
    
    @ManyToOne
    @JoinColumn(name = "CariID", nullable = false)
    private Cari cari;
    
    @Column(name = "FaturaTarihi")
    private LocalDateTime faturaTarihi = LocalDateTime.now();
    
    @Column(name = "FaturaNo", length = 50, nullable = false)
    private String faturaNo;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "Tur", length = 10, nullable = false)
    private FaturaTuru tur;
    
    @Column(name = "GenelToplam", precision = 15, scale = 2, nullable = false)
    private BigDecimal genelToplam;
    
    @Column(name = "Aciklama", length = 255)
    private String aciklama;
    
    @ManyToOne
    @JoinColumn(name = "ParaBirimi")
    private ParaBirimi paraBirimi;
    
    @Column(name = "Kur", precision = 18, scale = 6)
    private BigDecimal kur = BigDecimal.ONE;
    
    @OneToMany(mappedBy = "fatura", cascade = CascadeType.ALL)
    private List<StokGirisCikis> stokGirisCikislar;
    
    @OneToMany(mappedBy = "fatura", cascade = CascadeType.ALL)
    private List<FaturaHizmetKalemi> hizmetKalemleri;
}
