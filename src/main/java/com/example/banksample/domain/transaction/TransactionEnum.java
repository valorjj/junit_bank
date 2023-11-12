package com.example.banksample.domain.transaction;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum TransactionEnum {
    WITHDRAW("출금"),
    DEPOSIT("입금"),
    TRANSFER("이체"),
    LOG("입출금내역");

    private String value;

}
