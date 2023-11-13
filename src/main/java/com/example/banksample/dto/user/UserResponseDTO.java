package com.example.banksample.dto.user;

import com.example.banksample.domain.user.User;
import com.example.banksample.util.CustomDateUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

public class UserResponseDTO {

	private UserResponseDTO() {
	}

	@Getter
	@Setter
	public static class LoginResponseDTO {
		private Long id;
		private String username;
		private String createdAt;

		public LoginResponseDTO(User user) {
			this.id = user.getId();
			this.username = user.getUsername();
			this.createdAt = CustomDateUtil.toStringFormat(user.getCreatedAt());
		}
	}

	@Getter
	@Setter
	@ToString
	public static class JoinResponseDTO {
		private Long id;
		private String username;
		private String fullname;

		public JoinResponseDTO(User user) {
			this.id = user.getId();
			this.username = user.getUsername();
			this.fullname = user.getFullname();
		}

	}
}
