package com.example.banksample.handler;

import com.example.banksample.dto.ResponseDTO;
import com.example.banksample.handler.exception.CustomApiException;
import com.example.banksample.handler.exception.CustomForbiddenException;
import com.example.banksample.handler.exception.CustomValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class CustomExceptionHandler {

	@ExceptionHandler(CustomApiException.class)
	public ResponseEntity<?> apiException(CustomApiException ex) {
		log.error("error -> {}", ex.getMessage());
		return new ResponseEntity<>(new ResponseDTO<>(-1, ex.getMessage(), null), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(CustomForbiddenException.class)
	public ResponseEntity<?> forbiddenException(CustomForbiddenException ex) {
		log.error("error -> {}", ex.getMessage());
		return new ResponseEntity<>(new ResponseDTO<>(-1, ex.getMessage(), null), HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(CustomValidationException.class)
	public ResponseEntity<?> apiException(CustomValidationException ex) {
		log.error("error -> {}", ex.getMessage());
		return new ResponseEntity<>(new ResponseDTO<>(-1, ex.getLocalizedMessage(), ex.getErrorMap()), HttpStatus.BAD_REQUEST);
	}

}
