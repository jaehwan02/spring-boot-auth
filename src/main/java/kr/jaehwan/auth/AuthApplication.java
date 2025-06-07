package kr.jaehwan.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(exclude = {OAuth2ClientAutoConfiguration.class})
@EnableFeignClients(basePackages = "kr.jaehwan.auth.global.feign")
@ConfigurationPropertiesScan("kr.jaehwan.auth.global.config.properties")
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

}
