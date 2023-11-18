package com.example.banksample.web;

import com.example.banksample.auth.LoginUser;
import com.example.banksample.dto.ResponseDTO;
import com.example.banksample.service.AccountServiceV1;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import static com.example.banksample.dto.account.AccountRequestDTO.*;
import static com.example.banksample.dto.account.AccountResponseDTO.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AccountController {

	private final AccountServiceV1 accountServiceV1;

	@PostMapping("/test/account")
	public ResponseEntity<?> registerAccount(
			@RequestBody @Valid AccountSaveRequestDTO accountSaveRequestDTO,
			BindingResult bindingResult,
			@AuthenticationPrincipal LoginUser loginUser
	) {
		AccountSaveResponseDTO accountSaveResponseDTO = accountServiceV1.registerAccount(accountSaveRequestDTO, loginUser.getUser().getId());
		return new ResponseEntity<>(new ResponseDTO<>(1, "계좌가 성공적으로 생성되었습니다.", accountSaveResponseDTO), HttpStatus.CREATED);
	}


	/**
	 * {@code @PathVariable} 로 accountId 를 받지 않는다. 로그인 한 유저의 계좌와, 요청이 들어온 계좌가 동일한지 비교하는 로직이 필요하다.
	 * 비교 로직을 생략하고 동일한 목적을 달성하기 위해 아예 path variable 을 제거하는 방법을 사용한다.
	 */
	@GetMapping("/test/account/login-user")
	public ResponseEntity<?> findUserAccount(@AuthenticationPrincipal LoginUser loginUser) {
		AccountListResponseDTO accountListResponseDTO = accountServiceV1.getAccountsByUser(loginUser.getUser().getId());
		return new ResponseEntity<>(new ResponseDTO<>(1, "계좌 조회에 성공했습니다.", accountListResponseDTO), HttpStatus.OK);
	}


	/**
	 * 특정 계좌를 삭제한다.
	 */
	@DeleteMapping("/admin/account/{number}")
	public ResponseEntity<?> deleteAccount(@PathVariable Long number, @AuthenticationPrincipal LoginUser loginUser) {
		accountServiceV1.deleteAccount(number, loginUser.getUser().getId());
		return new ResponseEntity<>(new ResponseDTO<>(1, "계좌 삭제가 완료되었습니다.", null), HttpStatus.OK);
	}

	@PostMapping("/test/account/deposit")
	public ResponseEntity<?> depositAccount(
			@RequestBody @Valid AccountDepositRequestDTO accountDepositRequestDTO,
			BindingResult bindingResult
	) {
		AccountDepositResponseDTO accountDepositResponseDTO = accountServiceV1.depositAccount(accountDepositRequestDTO);
		return new ResponseEntity<>(new ResponseDTO<>(1, "계좌 입금이 완료되었습니다.", accountDepositResponseDTO), HttpStatus.CREATED);
	}


	/**
	 * @Valid 바로 뒤 파라미터로 BindingResult 가 위치해야 한다.
	 */
	@PostMapping("/test/account/withdraw")
	public ResponseEntity<?> withdrawAccount(@RequestBody @Valid WithdrawAccountRequestDTO accountWithdrawReqDto,
	                                         BindingResult bindingResult,
	                                         @AuthenticationPrincipal LoginUser loginUser) {
		TransferAccountResponseDTO accountWithdrawResponseDTO = accountServiceV1.withdrawAccount(accountWithdrawReqDto,
				loginUser.getUser().getId());
		return new ResponseEntity<>(new ResponseDTO<>(1, "계좌 출금이 완료되었습니다.", accountWithdrawResponseDTO), HttpStatus.CREATED);
	}


	@PostMapping("/test/account/transfer")
	public ResponseEntity<?> transferAccount(@RequestBody @Valid TransferAccountRequestDTO transferAccountRequestDTO,
	                                         BindingResult bindingResult,
	                                         @AuthenticationPrincipal LoginUser loginUser) {
		TransferAccountResponseDTO transferAccountResponseDTO = accountServiceV1.transferAccount(transferAccountRequestDTO,
				loginUser.getUser().getId());
		return new ResponseEntity<>(new ResponseDTO<>(1, "계좌 출금이 완료되었습니다.", transferAccountResponseDTO), HttpStatus.CREATED);
	}

}
