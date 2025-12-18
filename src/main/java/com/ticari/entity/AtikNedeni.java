package com.ticari.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "AtikNedenleri")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtikNedeni {
    
    @Id
    @Column(name = "NedenID")
    private Integer nedenId;
    
    @Column(name = "Aciklama", length = 100)
    private String aciklama;
    
    @OneToMany(mappedBy = "neden", cascade = CascadeType.ALL)
    private List<Atik> atiklar;
}
