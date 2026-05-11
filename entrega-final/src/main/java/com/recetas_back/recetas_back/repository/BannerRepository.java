package com.recetas_back.recetas_back.repository;

import com.recetas_back.recetas_back.model.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long> {
    List<Banner> findByActivoTrueOrderByOrdenAscIdAsc();
}
