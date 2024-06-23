package com.sovereingschool.back_streaming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class BackStreamingApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackStreamingApplication.class, args);
	}

}
