package com.wallet.quickpay.controller;

import com.wallet.quickpay.model.CreditRequest;
import com.wallet.quickpay.model.DebitRequest;
import com.wallet.quickpay.model.Transaction;
import com.wallet.quickpay.model.Wallet;
import com.wallet.quickpay.service.QuickPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class QuickPayController {
    @Autowired
    private QuickPayService quickPayService;
    @GetMapping("/wallets")
    public ResponseEntity<List<Wallet>> getAll(){
     return new ResponseEntity<>(quickPayService.getAllWallets(), HttpStatus.OK);
    }
    @PostMapping("/credit")
    public ResponseEntity<Transaction> credit(@Valid @RequestBody CreditRequest request)
    {
        return new ResponseEntity<>(quickPayService.credit(request),HttpStatus.CREATED);
    }
    @PostMapping("/debit")
    public ResponseEntity<Transaction> debit(@Valid @RequestBody DebitRequest request)
    {
        return new ResponseEntity<>(quickPayService.debit(request),HttpStatus.CREATED);
    }
    @GetMapping("/transactions")
    public ResponseEntity<?> transactions(@RequestParam(required = false) String walletId,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "5") int size)
    {
        Page<Transaction> txnPage;
        List<Transaction> transactions;
        Pageable pageable = PageRequest.of(page, size);
        if (walletId == null)
        {
            txnPage = quickPayService.getStatement(pageable);
        }
        else
        {
            txnPage = quickPayService.getStatementForWallet(walletId, pageable);
        }
        transactions = txnPage.getContent();

        if (transactions.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("transactions", transactions);
        response.put("currentPage", txnPage.getNumber());
        response.put("totalItems", txnPage.getTotalElements());
        response.put("totalPages", txnPage.getTotalPages());
        return new ResponseEntity<>(response,HttpStatus.OK);
    }
}
