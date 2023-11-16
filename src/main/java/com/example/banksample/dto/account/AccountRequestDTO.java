package com.example.banksample.dto.account;

import com.example.banksample.domain.account.Account;
import com.example.banksample.domain.user.User;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

public class AccountRequestDTO {

	private AccountRequestDTO() {
	}

	@Getter
	@Setter
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	public static class AccountSaveRequestDTO {

		@NotNull
		@Digits(integer = 4, fraction = 10)
		private Long accountNumber;

		@NotNull
		@Digits(integer = 6, fraction = 6)
		private Long accountPassword;

		public Account toEntity(User user) {
			return Account.builder()
				.number(accountNumber)
				.balance(1000L)
				.password(accountPassword)
				.user(user)
				.build();
		}
	}

}
