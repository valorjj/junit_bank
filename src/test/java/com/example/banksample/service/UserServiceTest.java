package com.example.banksample.service;

import com.example.banksample.domain.user.User;
import com.example.banksample.domain.user.UserEnum;
import com.example.banksample.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static com.example.banksample.service.UserServiceImplV1.*;
import static org.mockito.Mockito.*;

/**
 * Mockito 환경에서 테스트 진행
 */
@ExtendWith(MockitoExtension.class)
@Slf4j
class UserServiceTest {

    @InjectMocks // 가짜 환경을 주입받을 대상을 지정한다.
    private UserServiceImplV1 userServiceV1;

    @Mock // 메모리에 띄울 가짜 환경
    private UserRepository userRepository;

    // @Spy: 가짜 객체를 생성하는 @Mock 과 달리 스프링 컨테이너에 있는 실제 빈을 주입한다.
    @Spy
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Test
    @DisplayName("회원가입_테스트")
    void signUp_test() throws Exception {
        // given
        JoinRequestDTO joinRequestDTO = new JoinRequestDTO();
        joinRequestDTO.setUsername("jeongjin");
        joinRequestDTO.setFullname("kim jeongjin");
        joinRequestDTO.setEmail("admin@gmail.");
        joinRequestDTO.setPassword("1234");

        // stub_1
        // repository 에 대한 테스트가 아니므로, any() 를 인자로 넣는다.
        when(userRepository.findByUseranme(any())).thenReturn(Optional.empty());

        // stub_2
        // User 객체가 리턴되도록 한다.
        User user = User.builder()
            .id(1L)
            .username("jeongjin")
            .password("1234")
            .fullname("kim jeongjin")
            .role(UserEnum.CUSTOMER)
            .build();
        when(userRepository.save(any())).thenReturn(user);

        // when
        JoinResponseDTO joinResponseDTO = userServiceV1.signUp(joinRequestDTO);
        log.info("[*] joinResponseDTO -> {}", joinResponseDTO.toString());

        // then
        Assertions.assertThat(joinResponseDTO.getId()).isEqualTo(1L);
        Assertions.assertThat(joinResponseDTO.getUsername()).isEqualTo("jeongjin");
    }
}
