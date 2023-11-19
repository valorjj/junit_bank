package com.example.banksample.web;

import com.example.banksample.auth.LoginUser;
import com.example.banksample.dto.ResponseDTO;
import com.example.banksample.service.TransactionServiceV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.example.banksample.dto.transaction.TransactionResponseDTO.TransactionListResponseDTO;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class TransactionController {

	private final TransactionServiceV1 transactionServiceV1;

	@GetMapping("/test/account/{number}/transaction")
	public ResponseEntity<?> findTransactions(
			@PathVariable("number") Long accountNumber,
			@RequestParam(value = "type", defaultValue = "ALL") String type,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@AuthenticationPrincipal LoginUser loginUser
	) {
		TransactionListResponseDTO transactions = transactionServiceV1.findTransactions(loginUser.getUser().getId(), accountNumber, type, page);

		return new ResponseEntity<>(new ResponseDTO<>(1, "거래내역을 조회했습니다.", transactions), HttpStatus.OK);
	}


}
