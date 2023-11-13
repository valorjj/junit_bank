package com.example.banksample.config;

import com.example.banksample.jwt.JwtAuthenticationFilter;
import com.example.banksample.jwt.JwtAuthorizationFilter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

public class SecurityFilterManager extends AbstractHttpConfigurer<SecurityFilterManager, HttpSecurity> {

	@Override
	public void configure(HttpSecurity builder) throws Exception {
		AuthenticationManager authenticationManager = builder.getSharedObject(AuthenticationManager.class);
		builder.addFilter(new JwtAuthenticationFilter(authenticationManager));
		builder.addFilter(new JwtAuthorizationFilter(authenticationManager));
		super.configure(builder);
	}

}
