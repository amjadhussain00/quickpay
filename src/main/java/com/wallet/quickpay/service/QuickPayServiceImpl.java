package com.wallet.quickpay.service;

import com.wallet.quickpay.constant.TXN_TYPE;
import com.wallet.quickpay.dao.TransactionsDao;
import com.wallet.quickpay.dao.WalletsDao;
import com.wallet.quickpay.exception.ConcurrentRequestException;
import com.wallet.quickpay.exception.InsufficientFundException;
import com.wallet.quickpay.exception.WalletNotFoundException;
import com.wallet.quickpay.model.CreditRequest;
import com.wallet.quickpay.model.DebitRequest;
import com.wallet.quickpay.model.Transaction;
import com.wallet.quickpay.model.Wallet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;

@Slf4j
@Service
public class QuickPayServiceImpl implements QuickPayService {

    @Autowired
    private WalletsDao walletsDao;

    @Autowired
    private TransactionsDao transactionsDao;

    @Override
    public Transaction credit(CreditRequest request) {
        walletsDao.findById(request.getWalletId())
                .map(wallet -> {
                    wallet.setBalance(wallet.getBalance().add(request.getAmount()));
                    wallet.setUpdateTms(Calendar.getInstance().getTime());
                try {
                    return walletsDao.save(wallet);
                }catch (OptimisticLockingFailureException ex){
                    log.error("Concurrent requests were tried for wallet id {}",wallet.getId());
                    throw new ConcurrentRequestException(request.getWalletId());
                }
                }).orElseThrow(() -> new WalletNotFoundException(request.getWalletId()));

        Transaction txn = Transaction.builder()
                .txnTms(Calendar.getInstance().getTime())
                .walletId(request.getWalletId())
                .txnType(TXN_TYPE.CREDIT)
                .currency(request.getCurrency())
                .amount(request.getAmount())
                .build();
        return transactionsDao.save(txn);
    }

    @Override
    public Transaction debit(DebitRequest request) {
        walletsDao.findById(request.getWalletId())
                .map(wallet -> {
                    if(wallet.getBalance().compareTo(request.getAmount()) <0){
                            throw new InsufficientFundException(request.getWalletId());
                    }
                    wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));
                    wallet.setUpdateTms(Calendar.getInstance().getTime());
                    //sleepForSomeTime(request.getAmount());
                    try {
                         wallet = walletsDao.save(wallet);
                    }catch (OptimisticLockingFailureException ex){
                        log.error("Concurrent requests were tried for wallet id {}",wallet.getId());
                        throw new ConcurrentRequestException(request.getWalletId());
                    }
                    return wallet;
                 }).orElseThrow(() -> new WalletNotFoundException(request.getWalletId()));

        Transaction txn = Transaction.builder()
                .txnTms(Calendar.getInstance().getTime())
                .walletId(request.getWalletId())
                .txnType(TXN_TYPE.DEBIT)
                .currency(request.getCurrency())
                .amount(request.getAmount())
                .build();
        return transactionsDao.save(txn);
    }

    @Override
    public Page<Transaction> getStatementForWallet(String walletId, Pageable pageable) {
       return walletsDao.findById(walletId)
                .map(wallet -> transactionsDao.findByWalletId(wallet.getId(),pageable))
                .orElseThrow(() -> new WalletNotFoundException(walletId));
    }

    @Override
    public Page<Transaction> getStatement(Pageable pageable) {
        return transactionsDao.findAll(pageable);
    }

    @Override
    public List<Wallet> getAllWallets() {
        return walletsDao.findAll();
    }

}
