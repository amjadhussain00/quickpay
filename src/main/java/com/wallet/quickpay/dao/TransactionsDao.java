package com.wallet.quickpay.dao;

import com.wallet.quickpay.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionsDao extends JpaRepository<Transaction,Long> {

    Page<Transaction> findByWalletId(String walletId, Pageable pageable);
}
