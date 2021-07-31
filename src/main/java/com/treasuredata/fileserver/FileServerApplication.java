package com.treasuredata.fileserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FileServerApplication {

	public static void main(String[] args) {
		System.out.println("Starting File Server Application...");
		SpringApplication.run(FileServerApplication.class, args);
		System.out.println("Started File Server Application");
	}
}
