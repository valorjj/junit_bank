package com.example.banksample.web;

import com.example.banksample.dto.ResponseDTO;
import com.example.banksample.service.UserServiceV1;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.example.banksample.dto.user.UserRequestDTO.JoinRequestDTO;
import static com.example.banksample.dto.user.UserResponseDTO.JoinResponseDTO;

@RequestMapping("/api")
@RestController
@RequiredArgsConstructor

public class UserController {

    private final UserServiceV1 userServiceV1;

    @Operation(summary = "회원가입")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "회원가입 성공",
                    content = {
                            @Content(mediaType = "application/json")
                    })
    })
    @PostMapping("/signUp")
    public ResponseEntity<?> signUp(@RequestBody @Valid JoinRequestDTO joinRequestDTO, BindingResult bindingResult) {
        JoinResponseDTO joinResponseDTO = userServiceV1.signUp(joinRequestDTO);
        return new ResponseEntity<>(new ResponseDTO<>(1, "회원가입에 성공했습니다.", joinResponseDTO), HttpStatus.CREATED);
    }


}
