package com.berryselect.backend.auth.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "user_settings")
@Getter @Setter @NoArgsConstructor
public class UserSettings {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "allow_kakao_alert")
    private Boolean allowKakaoAlert;

    @Column(name = "marketing_option")
    private Boolean marketingOption;

    @Column(name = "notify_budget_alert")
    private Boolean notifyBudgetAlert;

    @Column(name = "notify_gifticon_expire")
    private Boolean notifyGifticonExpire;

    @Column(name = "notify_benefit_events")
    private Boolean notifyBenefitEvents;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void touchUpdate(){
        this.updatedAt = Instant.now();
    }
}
