package com.example.banksample.dto.account;

import com.example.banksample.domain.account.Account;
import com.example.banksample.domain.user.User;
import com.example.banksample.util.RegexCollection;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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


	@Getter
	@Setter
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
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
		@Pattern(regexp = RegexCollection.TRANSFER_TEL)
		private String tel;

	}



	@Getter
	@Setter
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	public static class WithdrawAccountRequestDTO {
		@NotNull
		@Digits(integer = 4, fraction = 10)
		private Long number;

		@NotNull
		@Digits(integer = 6, fraction = 6)
		private Long password;

		@NotNull
		private Long amount;

		@NotEmpty
		@Pattern(regexp = "WITHDRAW")
		private String type;
	}

	@Getter
	@Setter
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	public static class TransferAccountRequestDTO {

		@NotNull
		@Digits(integer = 4, fraction = 10)
		private Long withdrawNumber;

		@NotNull
		@Digits(integer = 4, fraction = 10)
		private Long depositNumber;

		@NotNull
		@Digits(integer = 6, fraction = 6)
		private Long withdrawPassword;

		@NotNull
		private Long amount;

		@NotEmpty
		@Pattern(regexp = "TRANSFER")
		private String type;
	}

}
