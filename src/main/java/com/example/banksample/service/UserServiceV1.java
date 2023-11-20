package com.example.banksample.service;

import static com.example.banksample.dto.user.UserRequestDTO.JoinRequestDTO;
import static com.example.banksample.dto.user.UserResponseDTO.JoinResponseDTO;

public interface UserServiceV1 {

    JoinResponseDTO signUp(JoinRequestDTO joinRequestDTO);



}
