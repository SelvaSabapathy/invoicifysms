package com.sms.invoicify;

import com.sms.invoicify.utilities.ExcludeGeneratedFromJaCoCo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InvoicifyApplication {

	@ExcludeGeneratedFromJaCoCo
	public static void main(String[] args) {
		SpringApplication.run(InvoicifyApplication.class, args);
	}

}
