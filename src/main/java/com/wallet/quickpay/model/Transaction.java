package com.wallet.quickpay.model;

import com.wallet.quickpay.constant.CURRENCY;
import com.wallet.quickpay.constant.TXN_TYPE;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;


@Entity
@Table(name = "TRANSACTION")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Transaction {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;
    @Column(name = "WALLET_ID")
    private String walletId;
    @Column(name = "TXN_TYPE")
    private TXN_TYPE txnType;
    @Column(name = "AMOUNT")
    private BigDecimal amount;
    @Column(name = "CURRENCY")
    private CURRENCY currency;
    @Column(name = "TXN_TMS")
    private Date txnTms;
}
