package com.berryselect.backend.wallet.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "brands")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Brand {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

//    @Convert(converter = StringListJsonConverter.class)
//    @Column(columnDefinition = "json")
//    private List<String> aliases;

    @Column(name = "category_id")
    private Long categoryId;

    @Builder.Default
    private Boolean isActive = true;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        final var now = LocalDateTime.now();
        if(createdAt == null) createdAt = now;
        if(updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
