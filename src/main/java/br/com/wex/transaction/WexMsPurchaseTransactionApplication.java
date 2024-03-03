package br.com.wex.transaction;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition
public class WexMsPurchaseTransactionApplication {

	public static void main(String[] args) {
		SpringApplication.run(WexMsPurchaseTransactionApplication.class, args);
	}

}
