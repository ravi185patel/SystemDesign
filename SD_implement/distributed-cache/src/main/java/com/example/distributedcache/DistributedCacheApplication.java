package com.example.distributedcache;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class DistributedCacheApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
		System.out.println("JAVA TZ = " + System.getProperty("user.timezone"));
		System.out.println("DEFAULT TZ = " + java.util.TimeZone.getDefault().getID());
		System.out.println("ZONE ID = " + java.time.ZoneId.systemDefault());

		SpringApplication.run(DistributedCacheApplication.class, args);
	}

}
