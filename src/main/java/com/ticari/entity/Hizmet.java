package com.ticari.entity;

import com.ticari.enums.HizmetTuru;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "Hizmetler")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Hizmet {
    
    @Id
    @Column(name = "HizmetID")
    private Integer hizmetId;
    
    @Column(name = "HizmetAd", length = 100, nullable = false)
    private String hizmetAd;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "Tur", length = 10)
    private HizmetTuru tur;
    
    @Column(name = "KDVOrani")
    private Integer kdvOrani = 20;
    
    @OneToMany(mappedBy = "hizmet", cascade = CascadeType.ALL)
    private List<FaturaHizmetKalemi> faturaHizmetKalemleri;
}
