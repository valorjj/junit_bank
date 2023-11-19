package com.example.banksample.dto.transaction;

import com.example.banksample.domain.account.Account;
import com.example.banksample.domain.transaction.Transaction;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class TransactionResponseDTO {

	private TransactionResponseDTO() {
	}


	@Getter
	@Setter
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	public static class TransactionListResponseDTO {
		private List<TransactionDTO> transactions = new ArrayList<>();

		public TransactionListResponseDTO(Account account, List<Transaction> transactions) {
			this.transactions = transactions.stream()
					.map(transaction -> new TransactionDTO(transaction, account.getNumber())).toList();
		}

		@Getter
		@Setter
		public class TransactionDTO {
			private Long id;
			private String type;
			private Long amount;
			private String sender;
			private String receiver;
			private String tel;
			private Long balance;

			public TransactionDTO(
					Transaction transaction,
					Long accountNumber
			) {
				this.id = transaction.getId();
				this.type = String.valueOf(transaction.getType());
				this.amount = transaction.getAmount();
				this.sender = transaction.getSender();
				this.receiver = transaction.getReceiver();
				this.tel = transaction.getTel() == null ? "없음" : transaction.getTel();

				// 출금계좌 null, 입금계좌 1001L
				if (transaction.getDepositAccount() == null) {
					this.balance = transaction.getWithdrawAccountBalance();
				}
				// 출금계좌 1001L, 입금계좌 null
				else if (transaction.getWithdrawAccount() == null) {
					this.balance = transaction.getDepositAccountBalance();
				}
				// 입, 출금 내역
				else {
					// 내가 찾는 계좌 == 입금 계좌
					if (accountNumber.longValue() == transaction.getDepositAccount().getNumber().longValue()) {
						this.balance = transaction.getDepositAccountBalance();
					}
					// 내가 찾는 계좌 == 출금 계좌
					else {
						this.balance = transaction.getWithdrawAccountBalance();
					}
				}
			}
		}
	}
}
