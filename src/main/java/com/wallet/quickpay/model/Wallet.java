package com.wallet.quickpay.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wallet.quickpay.constant.CURRENCY;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Table(name = "WALLET")
public class Wallet {
    @Id
    @Column(name = "ID")
    private String id;

    @JsonIgnore
    @Version
    @Column(name = "VERSION")
    private long version;
    @Column(name = "CURRENCY")
    private CURRENCY currency;
    @Column(name = "BALANCE")
    private BigDecimal balance;
    @Column(name = "CREATE_TMS")
    private Date createTms;
    @Column(name = "UPDATE_TMS")
    private Date updateTms;
}
