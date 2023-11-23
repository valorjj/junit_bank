package com.example.banksample.config;

import com.example.banksample.domain.user.UserEnum;
import com.example.banksample.util.CustomResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class SecurityConfig {

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		log.info("[디버그] BCryptPasswordEncoder 빈을 등록합니다.");
		return new BCryptPasswordEncoder();
	}

	/**
	 * Session 을 사용하지 않고 JWT 를 사용한다.
	 */
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity httpSecurity, HandlerMappingIntrospector introspector) throws Exception {
		MvcRequestMatcher.Builder mvcMatcherBuilder = new MvcRequestMatcher.Builder(introspector);
		// iframe 을 허용하지 않는다.
		httpSecurity.headers(authz -> authz.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));
		// csrf 가 작동하면 postman api 테스트를 할 수 없다.
		httpSecurity.csrf(AbstractHttpConfigurer::disable);
		//
		httpSecurity.cors(authz -> authz.configurationSource(configurationSource()));
		// JSessionID 를 서버에서 관리하지 않는다.
		httpSecurity.sessionManagement(authz -> authz.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		// React 와 같은 클라이언트로 요청한다.
		httpSecurity.formLogin(AbstractHttpConfigurer::disable);
		// httpBasic 은 브라우저가 팝업창을 이용해서 사용자 인증을 진행하는데, 허용하지 않는다.
		httpSecurity.httpBasic(AbstractHttpConfigurer::disable);
		// 서버로 요청을 받는 URL 패턴을 검사한다.
		httpSecurity.authorizeHttpRequests(authz ->
			authz
				.requestMatchers(mvcMatcherBuilder.pattern("/api/test/**")).authenticated()
				// 더 이상 ROLE_ prefix 를 사용하지 않는다.
				.requestMatchers(mvcMatcherBuilder.pattern("/api/admin/**")).hasRole(String.valueOf(UserEnum.ADMIN))
				.anyRequest().permitAll()
		);
		// 사용자가 정의한 커스텀 필터를 등록한다.
		httpSecurity.apply(new SecurityFilterManager());

		/*
		 * 인증 및 인과 과정 중 에러가 발생하는 상황에 커스텀 에러 객체를 리턴시킨다.
		 * */
		httpSecurity.exceptionHandling(authz -> authz
			.authenticationEntryPoint((req, res, ex) -> CustomResponseUtil.authFailed(res, "로그인이 필요합니다.", HttpStatus.UNAUTHORIZED))
			.accessDeniedHandler((req, res, ex) -> CustomResponseUtil.authFailed(res, "관리자 권한이 필요합니다.", HttpStatus.FORBIDDEN))
		);

		return httpSecurity.build();
	}

	public CorsConfigurationSource configurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		// 모든 HTTP 헤더를 허용한다.
		configuration.addAllowedHeader("*");
		// GET, POST, PUT, DELETE, OPTIONS 를 허용한다.
		configuration.addAllowedMethod("*");
		// 모든 IP 주소를 허용한다.
		configuration.addAllowedOriginPattern("*");
		// 클라이언트에서 쿠키 요청 허용
		configuration.setAllowCredentials(true);
		// 헤더 중 노출시킬 값을 지정한다.
		configuration.addExposedHeader("Authorization");

		/**
		 * 모든 엔드포인트는 도메인 + '/' 로 시작하기 때문에,
		 * '/**' 에 cors 관련 설정을 추가한다.
		 * */
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}


//	public class CustomSecurityFilterManager extends AbstractHttpConfigurer<CustomSecurityFilterManager, HttpSecurity> {
//		@Override
//		public void configure(HttpSecurity builder) throws Exception {
//			AuthenticationManager authenticationManager = builder.getSharedObject(AuthenticationManager.class);
//			builder.addFilter(new JwtAuthenticationFilter(authenticationManager));
//			builder.addFilter(new JwtAuthorizationFilter(authenticationManager));
//			super.configure(builder);
//		}
//
//	}


}
