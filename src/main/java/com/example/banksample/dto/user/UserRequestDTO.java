package com.example.banksample.dto.user;

import com.example.banksample.domain.user.User;
import com.example.banksample.domain.user.UserEnum;
import com.example.banksample.util.RegexCollection;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class UserRequestDTO {

	private UserRequestDTO() {
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class LoginRequestDTO {
		private String username;
		private String password;
	}


	@Getter
	@Setter
	@Builder
	public static class JoinRequestDTO {

		@NotEmpty
		@Pattern(regexp = RegexCollection.USER_NAME,
			message = "한글, 영문, 숫자 1~10자 이내로 작성해주세요"
		)
		private String username;

		@NotEmpty
		@Size(min = 4, max = 20)
		private String password;

		@NotEmpty
		@Pattern(regexp = RegexCollection.USER_EMAIL,
			message = "이메일 형식이 맞지 않습니다."
		)
		private String email;

		@NotEmpty
		@Pattern(regexp = RegexCollection.USER_FULL_NAME,
			message = "한글과 영문 2~20자 이내로 작성해주세요"
		)
		private String fullname;

		// DTO 를 Entity 로 변경하는 시점에 비밀번호 암호화
		public User toEntity(BCryptPasswordEncoder bCryptPasswordEncoder) {
			return User.builder()
				.username(username)
				.fullname(fullname)
				.email(email)
				.password(bCryptPasswordEncoder.encode(password))
				.role(UserEnum.CUSTOMER)
				.build();
		}
	}
}
