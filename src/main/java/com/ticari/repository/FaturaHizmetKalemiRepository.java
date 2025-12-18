package com.ticari.repository;

import com.ticari.entity.Fatura;
import com.ticari.entity.FaturaHizmetKalemi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaturaHizmetKalemiRepository extends JpaRepository<FaturaHizmetKalemi, Integer> {
    List<FaturaHizmetKalemi> findByFatura(Fatura fatura);
}
