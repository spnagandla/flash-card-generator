package com.flashcard;

import com.flashcard.config.InfisicalPropertySource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * The entry point of our Spring Boot application.
 * 
 * @SpringBootApplication is a convenience annotation that adds:
 * - @Configuration: Tags the class as a source of bean definitions.
 * - @EnableAutoConfiguration: Tells Spring Boot to start adding beans based on classpath settings.
 * - @ComponentScan: Tells Spring to look for other components, configurations, and services in the 'com.flashcard' package.
 */
@SpringBootApplication
@EnableAsync
public class FlashcardGeneratorApplication {

    public static void main(String[] args) {
        // This line launches the Spring application
        SpringApplication app = new SpringApplication(FlashcardGeneratorApplication.class);
        app.addInitializers(new InfisicalPropertySource());
        app.run(args);
    }
}
