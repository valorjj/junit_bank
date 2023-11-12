package com.example.banksample.service;

import com.example.banksample.domain.user.User;
import com.example.banksample.domain.user.UserEnum;
import com.example.banksample.handler.exception.CustomApiException;
import com.example.banksample.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImplV1 implements UserServiceV1 {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    /**
     * 서비스 레이어에서는 DTO 를 요청받고, DTO 로 응답한다.
     *
     * @param joinRequestDTO
     */
    @Override
    @Transactional
    public JoinResponseDTO signUp(JoinRequestDTO joinRequestDTO) {
        // 1. 동일한 사용자 이름이 존재하는지 검사
        Optional<User> userOptional = userRepository.findByUseranme(joinRequestDTO.getUsername());
        // 사용자 이름이 중복된 경우
        if (userOptional.isPresent()) {
            throw new CustomApiException("동일한 사용자 이름이 존재합니다.");
        }
        // 2. 패스워드 인코딩 + 회원가입 진행
        User userPS = userRepository.save(joinRequestDTO.toEntity(bCryptPasswordEncoder));
        // 3. DTO 응답
        return new JoinResponseDTO(userPS);
    }


    @Getter
    @Setter
    @ToString
    public static class JoinResponseDTO {
        private Long id;
        private String username;
        private String fullname;

        public JoinResponseDTO(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.fullname = user.getFullname();
        }

    }

    @Getter
    @Setter
    public static class JoinRequestDTO {
        private String username;
        private String password;
        private String email;
        private String fullname;

        // DTO 를 Entity 로 변경하는 시점에 비밀번호 암호화
        public User toEntity(BCryptPasswordEncoder bCryptPasswordEncoder) {
            return User.builder()
                .username(username)
                .fullname(fullname)
                .email(email)
                .password(bCryptPasswordEncoder.encode(password))
                .role(UserEnum.CUSTOMER)
                .build();
        }
    }
}
