package com.berryselect.backend.transaction.repository;

import com.berryselect.backend.transaction.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // 필요하면 추가 메소드 작성
}