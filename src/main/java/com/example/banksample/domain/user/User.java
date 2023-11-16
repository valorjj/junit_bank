package com.example.banksample.domain.user;

import com.example.banksample.common.BaseTime;
import com.example.banksample.domain.account.Account;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 엔티티로 등록된 User 객체 생성 시, new 를 사용하기 때문에 빈 생성자가 필요하다.
 * 단, DB 에 직접 영향을 미치는 객체기 때문에 임의로 생성되는 것을 막기 위해 Protected 접근 제한자를 부여한다.
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tbl_user")
@Getter
public class User extends BaseTime {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false, length = 20)
	private String username;

	// TODO: 패스워드 인코딩
	@Column(nullable = false, length = 60)
	private String password;

	@Column(nullable = false, length = 20)
	private String email;

	@Column(nullable = false, length = 20)
	private String fullname;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private UserEnum role;

	@OneToMany(mappedBy = "user")
	private List<Account> accounts = new ArrayList<>();

	public void add(Account account) {
		this.accounts.add(account);
	}

	@Builder
	public User(Long id, String username, String password, String email, String fullname, UserEnum role) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.email = email;
		this.fullname = fullname;
		this.role = role;
	}
}
