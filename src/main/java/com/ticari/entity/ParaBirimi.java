package com.ticari.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "ParaBirimleri")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParaBirimi {
    
    @Id
    @Column(name = "ParaKod", length = 3)
    private String paraKod;
    
    @Column(name = "Aciklama", length = 50)
    private String aciklama;
    
    @Column(name = "Sembol", length = 5)
    private String sembol;
    
    @OneToMany(mappedBy = "paraBirimi", cascade = CascadeType.ALL)
    private List<Kur> kurlar;
    
    @OneToMany(mappedBy = "paraBirimi", cascade = CascadeType.ALL)
    private List<Fatura> faturalar;
    
    @OneToMany(mappedBy = "paraBirimi", cascade = CascadeType.ALL)
    private List<StokGirisCikis> stokHareketleri;
    
    @OneToMany(mappedBy = "paraBirimi", cascade = CascadeType.ALL)
    private List<FaturaHizmetKalemi> faturaHizmetKalemleri;
    
    @OneToMany(mappedBy = "paraBirimi", cascade = CascadeType.ALL)
    private List<FinansHareketi> finansHareketleri;
}
