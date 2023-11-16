package com.example.banksample.dto.account;

import com.example.banksample.domain.account.Account;
import com.example.banksample.domain.user.User;
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


}
