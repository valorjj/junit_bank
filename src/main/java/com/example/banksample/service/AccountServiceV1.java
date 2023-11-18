package com.example.banksample.service;

import com.example.banksample.domain.account.Account;
import com.example.banksample.domain.transaction.Transaction;
import com.example.banksample.domain.transaction.TransactionEnum;
import com.example.banksample.domain.user.User;
import com.example.banksample.handler.exception.CustomApiException;
import com.example.banksample.repository.AccountRepository;
import com.example.banksample.repository.TransactionRepository;
import com.example.banksample.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.example.banksample.dto.account.AccountRequestDTO.*;
import static com.example.banksample.dto.account.AccountResponseDTO.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountServiceV1 {

	private final UserRepository userRepository;
	private final AccountRepository accountRepository;
	private final TransactionRepository transactionRepository;


	/*
	 * 사용자 Id 로 계좌를 조회한다.
	 */
	@Transactional
	public AccountListResponseDTO getAccountsByUser(Long userId) {
		User userPS = userRepository.findById(userId).orElseThrow(() -> new CustomApiException("해당 유저를 찾을 수 없습니다."));

		// 유저의 모든 계좌 목록
		List<Account> accountListPS = accountRepository.findByUser_id(userId);
		return new AccountListResponseDTO(userPS, accountListPS);
	}


	/*
	 * 새로운 계좌를 등록한다.
	 */
	@Transactional
	public AccountSaveResponseDTO registerAccount(AccountSaveRequestDTO accountSaveRequestDTO, Long userId) {
		// 사용자가 DB 에 존재하는지 확인
		User userPS = userRepository.findById(userId).orElseThrow(() -> new CustomApiException("해당 유저를 찾을 수 없습니다."));
		// 해당 계좌가 DB 에 존재하는지 여부 확인
		Optional<Account> accountOP = accountRepository.findByNumber(accountSaveRequestDTO.getAccountNumber());
		if (accountOP.isPresent()) {
			throw new CustomApiException("해당 계좌가 이미 존재합니다.");
		}
		// 계좌 등록
		Account accountPS = accountRepository.save(accountSaveRequestDTO.toEntity(userPS));

		// DTO 응답
		return new AccountSaveResponseDTO(accountPS);
	}

	/**
	 * 기존 계좌를 삭제한다.
	 */
	@Transactional
	public void deleteAccount(Long number, Long userId) {
		// 1. 계좌 확인
		Account accountPS = accountRepository.findByNumber(number).orElseThrow(() ->
				new CustomApiException("계좌를 찾을 수 없습니다."));
		// 2. 계좌 소유자 확인
		accountPS.checkOwner(userId);
		// 3. 계좌 삭제
		accountRepository.deleteById(accountPS.getId());
	}

	/**
	 * 계좌에 입금한다.
	 */
	@Transactional
	public AccountDepositResponseDTO depositAccount(AccountDepositRequestDTO accountDepositRequestDTO) {
		// 0원 체크
		if (accountDepositRequestDTO.getAmount() <= 0L) {
			throw new CustomApiException("0원 이하의 금액을 입금할 수 없습니다");
		}

		// 입금계좌 확인
		Account depositAccountPS = accountRepository.findByNumber(accountDepositRequestDTO.getNumber())
				.orElseThrow(
						() -> new CustomApiException("해당 계좌를 찾을 수 없습니다."));

		// 입금
		// 해당 계좌의 balance 를 조정한다.
		// -> update 쿼리를 날려서 더티 체킹
		depositAccountPS.deposit(accountDepositRequestDTO.getAmount());

		// 거래내역 남기기
		Transaction transaction = Transaction.builder()
				.depositAccount(depositAccountPS)
				.withdrawAccount(null)
				.depositAccountBalance(depositAccountPS.getBalance())
				.withdrawAccountBalance(null)
				.amount(accountDepositRequestDTO.getAmount())
				.type(TransactionEnum.DEPOSIT)
				.sender("ATM")
				.receiver(String.valueOf(depositAccountPS.getNumber()))
				.tel(accountDepositRequestDTO.getTel())
				.build();

		Transaction transactionPS = transactionRepository.save(transaction);
		return new AccountDepositResponseDTO(depositAccountPS, transactionPS);
	}


	/**
	 * 계좌에서 돈을 인출한다.
	 */
	@Transactional
	public TransferAccountResponseDTO withdrawAccount(WithdrawAccountRequestDTO withdrawAccountRequestDTO, Long userId) {
		// 0원 체크
		if (withdrawAccountRequestDTO.getAmount() <= 0L) {
			throw new CustomApiException("0원 이하의 금액을 출금 할 수 없습니다");
		}

		// 출금 확인
		Account withdrawAccountPS = accountRepository.findByNumber(withdrawAccountRequestDTO.getNumber())
				.orElseThrow(
						() -> new CustomApiException("해당 계좌를 찾을 수 없습니다."));

		// 출금 소유자 확인
		withdrawAccountPS.checkOwner(userId);

		// 출금 계좌 비밀번호 확인
		withdrawAccountPS.checkPassword(withdrawAccountRequestDTO.getPassword());

		// 출금
		withdrawAccountPS.withdraw(withdrawAccountRequestDTO.getAmount());

		// 거래내역
		// 사용자 계정에서 ATM 을 사용해 출금
		Transaction transaction = Transaction.builder()
				.depositAccount(null)
				.withdrawAccount(withdrawAccountPS)
				.depositAccountBalance(null)
				.withdrawAccountBalance(withdrawAccountPS.getBalance())
				.amount(withdrawAccountRequestDTO.getAmount())
				.type(TransactionEnum.WITHDRAW)
				.sender(String.valueOf(withdrawAccountRequestDTO.getNumber()))
				.receiver("ATM")
				.build();

		Transaction transactionPS = transactionRepository.save(transaction);

		// DTO 응답
		return new TransferAccountResponseDTO(withdrawAccountPS, transactionPS);
	}

	/**
	 * A 가 B 에게 입금한다.
	 */

	@Transactional
	public TransferAccountResponseDTO transferAccount(TransferAccountRequestDTO transferAccountRequestDTO, Long userId) {

		// 출금 계좌, 입금 계좌 동일한 지 여부 체크
		if (transferAccountRequestDTO.getWithdrawNumber().longValue() == transferAccountRequestDTO.getDepositNumber().longValue()) {
			throw new CustomApiException("입금 계좌와 출금 계좌가 동일할 수 없습니다.");
		}

		// 0원 체크
		if (transferAccountRequestDTO.getAmount() <= 0L) {
			throw new CustomApiException("0원 이하의 금액을 입금할 수 없습니다");
		}

		// 출금 계좌 확인
		Account withdrawAccountPS = accountRepository.findByNumber(transferAccountRequestDTO.getWithdrawNumber())
				.orElseThrow(
						() -> new CustomApiException("해당 계좌를 찾을 수 없습니다."));
		// 입금 계좌 확인
		Account depositAccountPS = accountRepository.findByNumber(transferAccountRequestDTO.getDepositNumber())
				.orElseThrow(
						() -> new CustomApiException("해당 계좌를 찾을 수 없습니다."));

		// 출금 소유자 확인 (로그인 된 사용자와 일치하는 지 여부)
		withdrawAccountPS.checkOwner(userId);

		// 출금 계좌 비밀번호 확인
		withdrawAccountPS.checkPassword(transferAccountRequestDTO.getWithdrawPassword());

		// 이체하기
		withdrawAccountPS.withdraw(transferAccountRequestDTO.getAmount());
		depositAccountPS.deposit(transferAccountRequestDTO.getAmount());

		// 거래내역
		Transaction transaction = Transaction.builder()
				.depositAccount(depositAccountPS)
				.withdrawAccount(withdrawAccountPS)
				.depositAccountBalance(depositAccountPS.getBalance())
				.withdrawAccountBalance(withdrawAccountPS.getBalance())
				.amount(transferAccountRequestDTO.getAmount())
				.type(TransactionEnum.TRANSFER)
				.sender(String.valueOf(transferAccountRequestDTO.getWithdrawNumber()))
				.receiver(String.valueOf(transferAccountRequestDTO.getDepositNumber()))
				.build();

		Transaction transactionPS = transactionRepository.save(transaction);

		// DTO 응답
		return new TransferAccountResponseDTO(withdrawAccountPS, transactionPS);
	}


}
