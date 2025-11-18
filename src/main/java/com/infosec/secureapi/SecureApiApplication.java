package com.infosec.secureapi;

import com.infosec.secureapi.config.EnvConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SecureApiApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SecureApiApplication.class);
        // Регистрируем ApplicationContextInitializer для загрузки .env файла
        app.addInitializers(new EnvConfig());
        app.run(args);
    }
}

