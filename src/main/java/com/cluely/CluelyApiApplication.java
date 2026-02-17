package com.cluely;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class CluelyApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CluelyApiApplication.class, args);
    }

}
