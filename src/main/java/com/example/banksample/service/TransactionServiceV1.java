package com.example.banksample.service;

import com.example.banksample.domain.account.Account;
import com.example.banksample.domain.transaction.Transaction;
import com.example.banksample.handler.exception.CustomApiException;
import com.example.banksample.repository.AccountRepository;
import com.example.banksample.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.banksample.dto.transaction.TransactionResponseDTO.TransactionListResponseDTO;

@Transactional(readOnly = true)
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceV1 {

	private final TransactionRepository transactionRepository;
	private final AccountRepository accountRepository;

	public TransactionListResponseDTO findTransactions(Long userid, Long accountNumber, String transactionType, int page) {
		// 1. 계좌 존재 여부 확인
		Account accountPS = accountRepository.findByNumber(accountNumber).orElseThrow(() -> new CustomApiException("해당 계좌를 찾을 수 없습니다."));
		// 2. 계좌 소유 여부 확인
		accountPS.checkOwner(userid);

		List<Transaction> transactionList = transactionRepository.findTransactionList(accountPS.getId(), transactionType, page);

		return new TransactionListResponseDTO(accountPS, transactionList);
	}


}
