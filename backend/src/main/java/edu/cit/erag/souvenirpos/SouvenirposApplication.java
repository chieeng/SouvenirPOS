package edu.cit.erag.souvenirpos;

import me.paulschwarz.springdotenv.spring.DotenvApplicationInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SouvenirposApplication {

    public static void main(String[] args) {
        // spring-dotenv 5.x no longer auto-registers, so wire it up explicitly. This loads
        // the backend/.env file into Spring's Environment before beans are created, so the
        // ${...} placeholders in application.properties resolve.
        SpringApplication app = new SpringApplication(SouvenirposApplication.class);
        app.addInitializers(new DotenvApplicationInitializer());
        app.run(args);
    }
}
