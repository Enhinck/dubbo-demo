package com.enhinck.monitor;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author xiaomi（huenbin ）
 * @since 2021-05-19 12:50
 */
@Configuration
@EnableAutoConfiguration
@EnableAdminServer
@SpringBootApplication
public class MonitorApplication {

	public static void main(String[] args) {

		SpringApplication.run(MonitorApplication.class, args);
	}
}