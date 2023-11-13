package com.example.banksample.jwt;

import com.example.banksample.auth.LoginUser;
import com.example.banksample.util.CustomResponseUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

import static com.example.banksample.dto.user.UserRequestDTO.LoginRequestDTO;
import static com.example.banksample.dto.user.UserResponseDTO.LoginResponseDTO;

@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private AuthenticationManager authenticationManager;

	public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
		super(authenticationManager);
		// default 로 지정되어 있는 POST '/login' 의 url 을 변경한다.
		setFilterProcessesUrl("/api/login");
		this.authenticationManager = authenticationManager;
	}

	// POST /api/login
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
		log.info("[*] attemptAuthentication 호출되었습니다.");
		try {
			ObjectMapper om = new ObjectMapper();
			LoginRequestDTO loginRequestDTO = om.readValue(request.getInputStream(), LoginRequestDTO.class);

			// 강제 로그인
			UsernamePasswordAuthenticationToken authenticationToken
				= new UsernamePasswordAuthenticationToken(loginRequestDTO.getUsername(), loginRequestDTO.getPassword());

			/*
			 * 아래 authenticate 메서드는 UserDetailsService 의 loadUserByUsername 을 호출한다.
			 * 세션을 강제 생성하는 이유는 jwt 를 사용하는 경우에도, 컨트롤러 진입 시점에
			 * 시큐리티 설정의 authorizeHttpRequest 의 도움을 받는 것이 편하기 때문이다.
			 * 강제 로그인으로 인한 세션의 생명 주기는 짧기 때문에 걱정 할 필요가 없다.
			 * request 시 생성되며, response 시 사라진다.
			 * */
			return authenticationManager.authenticate(authenticationToken);
		}
		// 시큐리티 로그인 과정 중 에러가 발생한 경우
		catch (Exception e) {
			log.error("error -> {}", e.getMessage());
			// 해당 에러 발생 시,
			// unsuccessfulAuthentication 메서드가 실행된다.
			throw new InternalAuthenticationServiceException(e.getMessage());
		}
	}

	/**
	 * InternalAuthenticationServiceException 에러 발생 시,
	 * 해당 메서드가 실행된다.
	 */
	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
		CustomResponseUtil.authFailed(response, "로그인에 실패했습니다.", HttpStatus.UNAUTHORIZED);
	}

	/**
	 * 인증 과정 중, {@code attemptAuthentication} 메서드를 통과하면
	 * 해당 메서드가 호출된다.
	 */
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
		log.info("[*] successfulAuthentication 호출되었습니다.");

		// 로그인 처리 된 유저 객체를 가져온다.
		LoginUser loginUser = (LoginUser) authResult.getPrincipal();
		// 유저 객체의 정보를 통해 jwt 토큰을 생성한다.
		String jwtToken = JwtProcess.createToken(loginUser);
		// 생성한 토큰을 응답 헤더에 추가한다.
		response.addHeader(JwtTokenVO.TOKEN_HEADER, jwtToken);
		LoginResponseDTO loginResponseDTO = new LoginResponseDTO(loginUser.getUser());

		CustomResponseUtil.loginSuccess(response, loginResponseDTO);
	}

}


