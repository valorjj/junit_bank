package com.example.banksample.config.dummy;

import com.example.banksample.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Profile("dev")
public class DummyDevInit extends DummyObject implements CommandLineRunner {
	private final UserRepository userRepository;

	@Override
	@Transactional
	public void run(String... args) throws Exception {
		userRepository.save(newUser("jeongjin", "kim jeongjin"));
	}
}
