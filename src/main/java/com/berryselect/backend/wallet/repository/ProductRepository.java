package com.berryselect.backend.wallet.repository;

import com.berryselect.backend.wallet.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
