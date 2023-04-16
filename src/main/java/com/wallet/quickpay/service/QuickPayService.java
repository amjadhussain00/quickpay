package com.wallet.quickpay.service;

import com.wallet.quickpay.model.CreditRequest;
import com.wallet.quickpay.model.DebitRequest;
import com.wallet.quickpay.model.Transaction;
import com.wallet.quickpay.model.Wallet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface QuickPayService {

    Transaction credit(CreditRequest request);

    Transaction debit(DebitRequest request);

    Page<Transaction> getStatementForWallet(String walletId, Pageable pageable);

    Page<Transaction> getStatement(Pageable pageable);

    List<Wallet> getAllWallets();
}
