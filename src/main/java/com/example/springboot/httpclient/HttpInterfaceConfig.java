package com.example.springboot.httpclient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
class HttpInterfaceConfig {
    private final String domain = "https://open.er-api.com";

    @Bean
    ErApi erApi() {
        WebClient webClient = WebClient.create(domain);
        WebClientAdapter adapter = WebClientAdapter.create(webClient);
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
                .builderFor(adapter)
                .build();

        return httpServiceProxyFactory.createClient(ErApi.class); // RestClient를 구현한 Bean을 만들어줌
    }
}