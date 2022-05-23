package com.paychex.voiceassistant;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
		servers = {
				@Server(url = "https://customizeThisURL.example.com", description = "Default Server URL")
		}
)
public class VoiceassistantApplication {

	public static void main(String[] args) {
		SpringApplication.run(VoiceassistantApplication.class, args);
	}

}
