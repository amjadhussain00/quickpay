package com.wallet.quickpay.model;

import com.wallet.quickpay.constant.CURRENCY;
import lombok.Data;

import java.math.BigDecimal;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

@Data
public class CreditRequest {

    @DecimalMin(value = "10.00", inclusive = true, message = "Amount should be minimum 10")
    @Digits(integer=5, fraction=2, message = "Amount supported up to 2 decimal places")
    @DecimalMax(value = "10000.00", inclusive = true, message = "Amount can be maximum 10000")
    @NotNull(message = "Amount can not be null")
    private BigDecimal amount;

    @NotNull(message = "Currency can not be null")
    private CURRENCY currency;

    @NotNull(message = "Wallet id can not be null")
    private String walletId;
}
