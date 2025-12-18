package com.ticari.repository;

import com.ticari.entity.AtikNedeni;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AtikNedeniRepository extends JpaRepository<AtikNedeni, Integer> {
}
