package com.teamup.teamUp;

import com.teamup.teamUp.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class TeamUpApplication {

	public static void main(String[] args) {
		SpringApplication.run(TeamUpApplication.class, args);
	}

}
