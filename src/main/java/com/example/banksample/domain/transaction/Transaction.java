package com.example.banksample.domain.transaction;


import com.example.banksample.common.BaseTime;
import com.example.banksample.domain.account.Account;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tbl_transaction")
@Getter
public class Transaction extends BaseTime {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private Account withdrawAccount;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private Account depositAccount;

	@Column(name = "amount", nullable = false)
	private Long amount;

	@Column(name = "withdraw_balance")
	private Long withdrawAccountBalance;

	@Column(name = "deposit_balance")
	private Long depositAccountBalance;

	/**
	 * WITHDRAW: 출금
	 * DEPOSIT: 입금
	 * TRANSFER: 이체
	 * LOG: 내역조회
	 */
	@Column(name = "type", nullable = false)
	@Enumerated(EnumType.STRING)
	private TransactionEnum type;


	public void setDepositAccount(Account depositAccount) {
		this.depositAccount = depositAccount;
	}

	public void setWithdrawAccount(Account withdrawAccount) {
		this.withdrawAccount = withdrawAccount;
	}

	/**
	 * 계좌가 삭제되는 경우도, 로그는 남아야한다.
	 */
	@Column(name = "tx_sender")
	private String sender;
	@Column(name = "tx_receiver")
	private String receiver;
	@Column(name = "tx_tel")
	private String tel;

	@Builder
	public Transaction(Long id, Account withdrawAccount, Account depositAccount, Long amount, Long withdrawAccountBalance, Long depositAccountBalance, TransactionEnum type, String sender, String receiver, String tel) {
		this.id = id;
		this.withdrawAccount = withdrawAccount;
		this.depositAccount = depositAccount;
		this.amount = amount;
		this.withdrawAccountBalance = withdrawAccountBalance;
		this.depositAccountBalance = depositAccountBalance;
		this.type = type;
		this.sender = sender;
		this.receiver = receiver;
		this.tel = tel;
	}
}
