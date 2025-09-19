package com.berryselect.backend.auth.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "user_consents",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","consent_type","version"}))
@Getter @Setter @NoArgsConstructor
public class UserConsent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "consent_type", nullable = false, length = 20)
    private ConsentType consentType;

    @Column(nullable = false, length = 20)
    private String version;

    @Column(nullable = false)
    private Boolean agreed;

    @Column(name="agreed_at", nullable = false)
    private Instant agreedAt;

    @Column(name = "source_ip", length = 45)
    private String sourceIp;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

}
