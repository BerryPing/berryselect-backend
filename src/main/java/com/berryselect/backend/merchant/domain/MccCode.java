package com.berryselect.backend.merchant.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mcc_codes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class MccCode {

    @Id
    private Integer mcc;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_category_id")
    private Category defaultCategory;
}