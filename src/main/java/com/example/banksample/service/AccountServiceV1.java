package com.example.banksample.service;

import com.example.banksample.domain.account.Account;
import com.example.banksample.domain.user.User;
import com.example.banksample.handler.exception.CustomApiException;
import com.example.banksample.repository.AccountRepository;
import com.example.banksample.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.example.banksample.dto.account.AccountRequestDTO.AccountSaveRequestDTO;
import static com.example.banksample.dto.account.AccountResponseDTO.AccountListResponseDTO;
import static com.example.banksample.dto.account.AccountResponseDTO.AccountSaveResponseDTO;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountServiceV1 {

	private final UserRepository userRepository;
	private final AccountRepository accountRepository;


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


}
