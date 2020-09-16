package com.maersk.wnd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.maersk.*"})
public class WndSpringBootTemplateProviderApplication {

	public static void main(String[] args) {
		SpringApplication.run(WndSpringBootTemplateProviderApplication.class, args);
	}

}
