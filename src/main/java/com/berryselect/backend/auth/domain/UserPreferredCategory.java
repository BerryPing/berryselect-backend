package com.berryselect.backend.auth.domain;

import com.berryselect.backend.merchant.domain.Category;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_preferred_categories",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","category_id"}))
@Getter @Setter @NoArgsConstructor
public class UserPreferredCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // user_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // category_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

}
