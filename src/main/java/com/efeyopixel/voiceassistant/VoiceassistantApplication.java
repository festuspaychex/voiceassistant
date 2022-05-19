package com.efeyopixel.voiceassistant;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
		servers = {
				@Server(url = "https://2dd7-159-235-182-65.ngrok.io", description = "Default Server URL")
		}
)
public class VoiceassistantApplication {

	public static void main(String[] args) {
		SpringApplication.run(VoiceassistantApplication.class, args);
	}

}
