package com.sovereingschool.back_chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan(basePackages = "com.sovereingschool.back_common.Models")
@EnableJpaRepositories(basePackages = {
		"com.sovereingschool.back_chat.Repositories", // si aún usás repos locales
		"com.sovereingschool.back_common.Repositories" // para los repos que moviste
})
@ComponentScan(basePackages = {
		"com.sovereingschool.back_chat",
		"com.sovereingschool.back_common"
})
@SpringBootApplication
public class BackChatApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackChatApplication.class, args);
	}
}
