package com.example.banksample.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ResponseDTO<T> {
    /**
     * 1: 성공
     * -1: 실패
     */
    private final Integer code;

    private final String message;
    private final T data;
}
