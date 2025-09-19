package com.berryselect.backend.wallet.repository;

import com.berryselect.backend.wallet.domain.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Long> {
}
