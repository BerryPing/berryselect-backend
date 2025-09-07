package com.berryselect.backend.auth.repository;

import com.berryselect.backend.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByProviderAndProviderUserId(User.Provider provider, String providerUserId);
}
