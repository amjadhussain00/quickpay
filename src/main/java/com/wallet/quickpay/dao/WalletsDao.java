package com.wallet.quickpay.dao;

import com.wallet.quickpay.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletsDao extends JpaRepository<Wallet,String> {
}
