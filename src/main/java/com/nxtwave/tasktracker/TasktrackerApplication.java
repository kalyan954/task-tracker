package com.nxtwave.tasktracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class TasktrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TasktrackerApplication.class, args);
	}

}
