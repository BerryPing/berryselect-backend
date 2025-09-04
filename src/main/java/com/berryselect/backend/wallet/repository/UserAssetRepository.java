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
    List<UserAsset> findByUserId(Long userId);

    @EntityGraph(attributePaths = "product")
    List<UserAsset> findByUserIdAndAssetTypeOrderByIdDesc(Long userId, AssetType assetType);

    @EntityGraph(attributePaths = "product")
    Optional<UserAsset> findByIdAndUserIdAndAssetType(Long id, Long userId, AssetType assetType);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = "product")
    Optional<UserAsset> findWithLockByIdAndUserIdAndAssetType(Long id, Long userId, AssetType assetType);

    @EntityGraph(attributePaths = "product")
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
