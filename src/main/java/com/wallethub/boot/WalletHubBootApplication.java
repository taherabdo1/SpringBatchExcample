package com.wallethub.boot;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableBatchProcessing
public class WalletHubBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(WalletHubBootApplication.class, args);
	}
}
