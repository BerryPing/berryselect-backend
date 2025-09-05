package com.berryselect.backend.wallet.repository;

import com.berryselect.backend.wallet.domain.UserAsset;
import com.berryselect.backend.wallet.domain.type.AssetType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserAssetRepository extends JpaRepository<UserAsset, Long> {
    List<UserAsset> findByUserId(Long userId);

    @EntityGraph(attributePaths = "product")
    List<UserAsset> findByUserIdAndAssetTypeOrderByIdDesc(Long userId, AssetType assetType);

    @EntityGraph(attributePaths = "product")
    Optional<UserAsset> findByIdAndUserIdAndAssetType(Long id, Long userId, AssetType assetType);

    @EntityGraph(attributePaths = "product")
    Optional<UserAsset> findByIdWithProduct(Long id);

    @Query("""
        SELECT ua.id, p.name 
        FROM UserAsset ua 
        LEFT JOIN Product p ON ua.productId = p.id 
        WHERE ua.id IN :assetIds
        """)
    List<Object[]> findNamesByIds(@Param("assetIds") List<Long> assetIds);
}
