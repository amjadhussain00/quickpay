package com.wallet.quickpay.exception;

public class ConcurrentRequestException extends RuntimeException{
    public ConcurrentRequestException(String id){
        super(String.format("Another request was processed for Wallet %s, please retry",id));
    }
}
