package com.ticari.repository;

import com.ticari.entity.ParaBirimi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParaBirimiRepository extends JpaRepository<ParaBirimi, String> {
}
