package com.example.banksample.domain.account;

import com.example.banksample.common.BaseTime;
import com.example.banksample.domain.user.User;
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

    @Column(unique = true, nullable = false, length = 12)
    private Long number;        // 계좌번호
    @Column(nullable = false, length = 6)
    private Long password;      // 계좌 비밀번호
    @Column(nullable = false)
    private Long balance;       // 잔액 (기본값을 1000 으로 부여)

    /**
     * account.getUser().어떤필드() => 이 시점에 Lazy 로딩 발생!
     * 즉, @Getter 를 통해서 내가 제어권을 획득한다.
     * */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

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
}
