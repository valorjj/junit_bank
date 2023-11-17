package com.example.banksample.service;

import com.example.banksample.domain.account.Account;
import com.example.banksample.domain.transaction.Transaction;
import com.example.banksample.domain.transaction.TransactionEnum;
import com.example.banksample.domain.user.User;
import com.example.banksample.handler.exception.CustomApiException;
import com.example.banksample.repository.AccountRepository;
import com.example.banksample.repository.TransactionRepository;
import com.example.banksample.repository.UserRepository;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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

	@Getter
	@Setter
	public static class AccountDepositResponseDTO {
		private Long id;                        // 계좌 ID
		private Long number;                    // 계좌번호
		private TransactionDTO transaction;  // 거래내역 DTO

		/*
		* TransactionDTO 가 아닌 Transaction 을 전달하면 어떻게 될까?
		* 1. 서비스 레이어에서 컨트롤러 레이어로 이동 시, 엔티티를 직접적으로 노출하지 않는다.
		* 2. 양방향 맵핑이 되어 있는 경우, 지연로딩이 발생하는 경우 순환참조 에러가 발생한다.
		* */

		public AccountDepositResponseDTO(Account account, Transaction transaction) {
			this.id = account.getId();
			this.number = account.getNumber();
			this.transaction = new TransactionDTO(transaction);
		}

		@Getter
		@Setter
		public class TransactionDTO {
			private Long id;
			private String type;
			private String sender;
			private String receiver;
			private Long amount;
			/*
			 * 클라이언트에게 전달하지 않는 값이다.
			 * 서버에서 확인 용도로만 사용한다.
			 * */
			@JsonIgnore
			private Long depositAccountBalance;
			private String tel;

			public TransactionDTO(Transaction transaction) {
				this.id = transaction.getId();
				this.type = transaction.getType().name();
				this.sender = transaction.getSender();
				this.receiver = transaction.getReceiver();
				this.amount = transaction.getAmount();
				this.depositAccountBalance = transaction.getDepositAccountBalance();
				this.tel = transaction.getTel();
			}
		}
	}


	@Getter
	@Setter
	public static class AccountDepositRequestDTO {
		@NotNull
		@Digits(integer = 4, fraction = 10)
		private Long number;

		// 0원 검사
		@NotNull
		private Long amount;

		/*
		 * 'DEPOSIT' 단어만을 허용한다.
		 * */
		@NotEmpty
		@Pattern(regexp = "DEPOSIT")
		private String type;

		/*
		 * 하이픈 없이
		 * */
		@NotEmpty
		@Pattern(regexp = "^\\d{11}")
		private String tel;

	}


}
