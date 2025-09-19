package com.berryselect.backend.wallet.repository;

import com.berryselect.backend.wallet.domain.Product;
import com.berryselect.backend.wallet.domain.type.AssetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // 단건 조회
    @EntityGraph(attributePaths = {"brandRef"})
    Optional<Product> findWithBrandRefById(Long id);

    // 타입별 페이징 (CARD/MEMBERSHIP/GIFTICON)
    Page<Product> findByProductType(AssetType productType, Pageable pageable);

    // 표준 브랜드 FK(brand_id)로 필터링
    List<Product> findByBrandRef_Id(Long brandId);

    // 브랜드 묶음 조회 필터링
    List<Product> findByBrandRef_IdIn(Collection<Long> brandIds);

    // 브랜드 ‘이름’으로 필터링
    @Query("""
      select p from Product p
      join p.brandRef b
      where b.name = :brandName
    """)
    List<Product> findByBrandName(String brandName);

    // OCR/사용자 입력 원본 문자열로 검색
    List<Product> findByBrandContainingIgnoreCase(String brand);
}
