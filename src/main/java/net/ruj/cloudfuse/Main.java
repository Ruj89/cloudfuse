package net.ruj.cloudfuse;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;

import java.util.List;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        new SpringApplicationBuilder(Main.class).run(args);
    }

    @Bean
    public OAuth2RestTemplate oauth2RestTemplate(OAuth2ProtectedResourceDetails resource, OAuth2ClientContext context) {
        OAuth2RestTemplate oAuth2RestTemplate = new OAuth2RestTemplate(resource, context);
        List<HttpMessageConverter<?>> converters = oAuth2RestTemplate.getMessageConverters();
        converters.add(new ResourceHttpMessageConverter());
        oAuth2RestTemplate.setMessageConverters(converters);
        return oAuth2RestTemplate;
    }
}
