package com.example.banksample.domain.account;

import com.example.banksample.common.BaseTime;
import com.example.banksample.domain.user.User;
import com.example.banksample.handler.exception.CustomApiException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tbl_account")
@Getter
public class Account extends BaseTime {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false, length = 10)
	private Long number;        // 계좌번호

	@Column(nullable = false, length = 6)
	private Long password;      // 계좌 비밀번호

	@Column(nullable = false)
	private Long balance;       // 잔액 (기본값을 1000 으로 부여)

	/*
	 * account.getUser().어떤필드() => 이 시점에 Lazy 로딩 발생!
	 * 즉, @Getter 를 통해서 내가 제어권을 획득한다.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_account_to_user"))
	private User user;


	/**
	 * User 와 양방향 맵핑을 맺기 위해,
	 * 영속성 컨텍스트에 저장된 Account 에 User 객체를 주입한다.
	 * User 엔티티의 @Id 값이 맵핑된다. 따로 지정하지 않는다면 user_id 라는 컬럼이 생성 & 맵핑된다.
	 */
	public void setUser(User user) {
		this.user = user;
	}

	@Builder
	public Account(Long id, Long number, Long password, Long balance, User user) {
		this.id = id;
		this.number = number;
		this.password = password;
		this.balance = balance;
		this.user = user;
	}

	/**
	 * 계좌 소유자 일치 여부 확인
	 */
	public void checkOwner(Long userId) {
		// id 를 조회하는 경우 지연로딩 발생하지 않는다.
		if (this.user.getId().longValue() != userId.longValue()) {
			throw new CustomApiException("계좌의 소유자가 아닙니다.");
		}
	}

	/*
	 * 계좌 입금
	 * */
	public void deposit(Long amount) {
		this.balance += amount;
	}

	public void checkPassword(Long password) {
		if (this.password.longValue() != password.longValue()) {
			throw new CustomApiException("계좌 비밀번호가 일치하지 않습니다.");
		}
	}

	public void checkBalance(Long amount) {
		if (this.balance < amount) {
			throw new CustomApiException("계좌 잔액이 부족합니다.");
		}
	}

	public void withdraw(Long amount) {
		checkBalance(amount);
		this.balance -= amount;
	}

}
