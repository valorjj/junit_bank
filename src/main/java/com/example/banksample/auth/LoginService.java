package com.example.banksample.auth;

import com.example.banksample.domain.user.User;
import com.example.banksample.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService implements UserDetailsService {
	private final UserRepository userRepository;

	/**
	 * 로그인 시, 해당 메서드를 실행해서 username 을 체크한다.
	 * username 이 존재한다면 시큐리티 컨텍스트 내부에 로그인 완료된 세션이 생성된다.
	 * 없다면 오류를 발생시킨다.
	 */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User userPS = userRepository.findByUsername(username).orElseThrow(
			() -> new InternalAuthenticationServiceException("[" + username + "] 인증에 실패했습니다.")
		);
		return new LoginUser(userPS);
	}
}
