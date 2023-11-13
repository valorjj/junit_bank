package com.example.banksample.service;

import com.example.banksample.dto.user.UserRequestDTO;
import com.example.banksample.dto.user.UserResponseDTO;

public interface UserServiceV1 {

    UserResponseDTO.JoinResponseDTO signUp(UserRequestDTO.JoinRequestDTO joinRequestDTO);



}
