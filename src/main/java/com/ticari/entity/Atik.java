package com.ticari.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "Atiklar")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Atik {
    
    @Id
    @Column(name = "AtikID")
    private Integer atikId;
    
    @ManyToOne
    @JoinColumn(name = "UrunID")
    private Urun urun;
    
    @ManyToOne
    @JoinColumn(name = "NedenID")
    private AtikNedeni neden;
    
    @Column(name = "Miktar")
    private Integer miktar;
    
    @Column(name = "Tarih")
    private LocalDateTime tarih = LocalDateTime.now();
}
