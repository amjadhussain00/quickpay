package com.wallet.quickpay.exception;

public class InsufficientFundException extends RuntimeException {
    public InsufficientFundException(String id){
        super(String.format("Wallet %s does not have sufficient fund",id));
    }
}
