package dev.seqism.ex.processor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"dev.seqism"})
@SpringBootApplication
public class Main {
	public static void main(final String[] args) {
		SpringApplication.run(Main.class, args);
	}
}
