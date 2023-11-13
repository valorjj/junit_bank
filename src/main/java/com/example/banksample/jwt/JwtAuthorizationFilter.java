package com.example.banksample.jwt;

import com.example.banksample.auth.LoginUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.IOException;

/**
 * 토큰을 검증하는 역할을 맡는다.
 */
@Slf4j
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

	public JwtAuthorizationFilter(AuthenticationManager authenticationManager) {
		super(authenticationManager);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
		// 토큰이 존재하는 경우
		if (isHeaderValid(request, response)) {
			// 헤더에서 토큰을 추출한다.
			String token = request.getHeader(JwtTokenVO.TOKEN_HEADER).replace(JwtTokenVO.TOKEN_PREFIX, "");
			LoginUser loginUser = JwtProcess.verifyToken(token);
			/*
			 * 여기까지 온 경우, 해당 유저는 인증이 된 상태이다.
			 * 임시로 세션을 생성하기 위해 UsernamePasswordAuthenticationToken 객체를 생성한다.
			 * 단, 생성자마다 파라미터가 다르기 때문에 주의가 필요하다.
			 * 그리고 중요한 것은 authorities 이다. authorizeHttpRequest 에 설정한
			 * 여러 조건들을 통과하는지 여부가 중요하기 때문이다.
			 * */
			Authentication authentication = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
			// 생성한 세션을 컨텍스트에 주입한다.
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
		// doFilter 를 조건문 안에 넣는 실수를 조심하자. 해당 메서드는 반드시 실행되어야 한다.
		chain.doFilter(request, response);
	}

	/**
	 * 토큰 헤더가 {@code Authorization: Bearer ...} 형식이 맞는지 검사한다.
	 */
	public boolean isHeaderValid(HttpServletRequest request, HttpServletResponse response) {
		String header = request.getHeader(JwtTokenVO.TOKEN_HEADER);
		return header != null && header.startsWith(JwtTokenVO.TOKEN_PREFIX);
	}
}
