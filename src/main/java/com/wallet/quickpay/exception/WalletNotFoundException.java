package com.wallet.quickpay.exception;

public class WalletNotFoundException extends RuntimeException {
    public WalletNotFoundException(String id){
        super(String.format("Wallet %s does not exist",id));
    }
}
