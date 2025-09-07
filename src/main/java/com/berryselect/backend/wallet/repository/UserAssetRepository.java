package com.berryselect.backend.wallet.repository;

import com.berryselect.backend.wallet.domain.UserAsset;
import com.berryselect.backend.wallet.domain.type.AssetType;
import com.berryselect.backend.wallet.domain.type.GifticonStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserAssetRepository extends JpaRepository<UserAsset, Long> {

    // Product + 표준 브랜드 FK(brand_id) 조회
    @EntityGraph(attributePaths = {"product", "product.brandRef"})
    List<UserAsset> findByUserId(Long userId);

    // 타입별(CARD/MEMBERSHIP/GIFTICON) 조회
    @EntityGraph(attributePaths = {"product", "product.brandRef"})
    List<UserAsset> findByUserIdAndAssetTypeOrderByIdDesc(Long userId, AssetType assetType);

    // 단건 상세 조회
    @EntityGraph(attributePaths = {"product", "product.brandRef"})
    Optional<UserAsset> findByIdAndUserIdAndAssetType(Long id, Long userId, AssetType assetType);

    // 기프티콘 사용 처리
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"product", "product.brandRef"})
    Optional<UserAsset> findWithLockByIdAndUserIdAndAssetType(Long id, Long userId, AssetType assetType);

    // 기프티콘 목록 검색(상세 필터, 만료기간 필터)
    @EntityGraph(attributePaths = {"product", "product.brandRef"})
    @Query("""
        select ua
          from UserAsset ua
         where ua.userId = :userId
           and ua.assetType = com.berryselect.backend.wallet.domain.type.AssetType.GIFTICON
           and (:status is null or ua.gifticonStatus = :status)
           and (
                (:from is null and :to is null)
                or (ua.expiresAt is not null and ua.expiresAt between :from and :to)
           )
        """)
    Page<UserAsset> searchGifticons(@Param("userId") Long userId,
                                    @Param("status")GifticonStatus status,
                                    @Param("from")LocalDate from,
                                    @Param("to") LocalDate to,
                                    Pageable pageable);

}
