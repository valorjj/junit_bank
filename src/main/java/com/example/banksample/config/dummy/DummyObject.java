package com.example.banksample.config.dummy;

import com.example.banksample.domain.user.User;
import com.example.banksample.domain.user.UserEnum;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class DummyObject {

    /**
     * 엔티티를 데이터베이스에 저장할 때 사용한다.
     *
     * @param username
     * @param fullname
     * @return
     */
    protected User newUser(String username, String fullname) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encPassword = passwordEncoder.encode("1234");
        return User.builder()
            .username(username)
            .email("mockuser@nate.com")
            .password(encPassword)
            .fullname(fullname)
            .role(UserEnum.CUSTOMER)
            .build();
    }

    /**
     * 테스트 객체에서 stub 용도로 사용한다.
     *
     * @param id
     * @param username
     * @param fullname
     * @return
     */
    protected User newMockUser(Long id, String username, String fullname) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encPassword = passwordEncoder.encode("1234");
        return User.builder()
            .id(id)
            .username(username)
            .email("mockuser@nate.com")
            .password(encPassword)
            .fullname(fullname)
            .role(UserEnum.CUSTOMER)
            .build();
    }

}
