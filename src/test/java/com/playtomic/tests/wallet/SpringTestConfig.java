package com.playtomic.tests.wallet;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class SpringTestConfig {

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}