package com.example.banksample.dto.account;

import com.example.banksample.domain.account.Account;
import com.example.banksample.domain.transaction.Transaction;
import com.example.banksample.domain.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class AccountResponseDTO {

	private AccountResponseDTO() {

	}

	@Getter
	@Setter
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	public static class AccountSaveResponseDTO {
		private Long id;
		private Long number;
		private Long balance;

		public AccountSaveResponseDTO(Account account
		) {
			this.id = account.getId();
			this.number = account.getNumber();
			this.balance = account.getBalance();
		}
	}

	@Getter
	@Setter
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	public static class AccountListResponseDTO {
		private String fullname;
		private List<AccountDTO> accounts;

		public AccountListResponseDTO(User user, List<Account> accounts) {
			this.fullname = user.getFullname();
			this.accounts = accounts.stream().map(AccountDTO::new).toList();
		}

		@Getter
		@Setter
		public class AccountDTO {
			private Long id;
			private Long balance;
			private Long number;

			public AccountDTO(Account account) {
				this.id = account.getId();
				this.balance = account.getBalance();
				this.number = account.getNumber();
			}
		}
	}


	@Getter
	@Setter
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	public static class AccountDepositResponseDTO {
		private Long id;                        // 계좌 ID
		private Long number;                    // 계좌번호
		private TransactionDTO transaction;     // 거래내역 DTO

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
				this.type = String.valueOf(transaction.getType());
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
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	public static class AccountWithdrawResponseDTO {
		private Long id;                        // 계좌 ID
		private Long number;                    // 계좌번호
		private Long balance;                   // 계좌잔액
		private TransactionDTO transaction;     // 거래내역 DTO

		/*
		 * TransactionDTO 가 아닌 Transaction 을 전달하면 어떻게 될까?
		 * 1. 서비스 레이어에서 컨트롤러 레이어로 이동 시, 엔티티를 직접적으로 노출하지 않는다.
		 * 2. 양방향 맵핑이 되어 있는 경우, 지연로딩이 발생하는 경우 순환참조 에러가 발생한다.
		 * */

		public AccountWithdrawResponseDTO(Account account, Transaction transaction) {
			this.id = account.getId();
			this.number = account.getNumber();
			this.balance = account.getBalance();
			this.transaction = new TransactionDTO(transaction);
		}

		@Getter
		@Setter
		@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
		public class TransactionDTO {
			private Long id;
			private String type;
			private String sender;
			private String receiver;
			private Long amount;

			public TransactionDTO(Transaction transaction) {
				this.id = transaction.getId();
				this.type = String.valueOf(transaction.getType());
				this.sender = transaction.getSender();
				this.receiver = transaction.getReceiver();
				this.amount = transaction.getAmount();
			}
		}
	}


	@Getter
	@Setter
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	public static class TransferAccountResponseDTO {
		private Long id;                        // 출금 계좌 ID
		private Long number;                    // 출금 계좌번호
		private Long balance;                   // 출금 계좌잔액
		private TransactionDTO transaction;     // 거래내역 DTO

		/*
		 * TransactionDTO 가 아닌 Transaction 을 전달하면 어떻게 될까?
		 * 1. 서비스 레이어에서 컨트롤러 레이어로 이동 시, 엔티티를 직접적으로 노출하지 않는다.
		 * 2. 양방향 맵핑이 되어 있는 경우, 지연로딩이 발생하는 경우 순환참조 에러가 발생한다.
		 * */

		public TransferAccountResponseDTO(Account account, Transaction transaction) {
			this.id = account.getId();
			this.number = account.getNumber();
			this.balance = account.getBalance();
			this.transaction = new TransactionDTO(transaction);
		}

		@Getter
		@Setter
		@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
		public class TransactionDTO {
			private Long id;
			private String type;
			private String sender;
			private String receiver;
			private Long amount;
			// 입금계좌 잔액은 외부 노출은 하지 않는다. 서버에서 확인용도로 사용한다.
			@JsonIgnore
			private Long depositAccountBalance;


			public TransactionDTO(Transaction transaction) {
				this.id = transaction.getId();
				this.type = String.valueOf(transaction.getType());
				this.sender = transaction.getSender();
				this.receiver = transaction.getReceiver();
				this.depositAccountBalance = transaction.getDepositAccountBalance();
				this.amount = transaction.getAmount();
			}
		}
	}


}
