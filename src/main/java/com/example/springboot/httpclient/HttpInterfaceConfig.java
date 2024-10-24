package com.example.springboot.httpclient;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@RequiredArgsConstructor
class HttpInterfaceConfig {

    private final RestClient restClient;

//    @Bean
//    ErApi erApi() {
//        WebClient webClient = WebClient.create(Domain.ORIGIN);
//        WebClientAdapter adapter = WebClientAdapter.create(webClient);
//        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
//                .builderFor(adapter)
//                .build();
//
//        return httpServiceProxyFactory.createClient(ErApi.class); // RestClient를 구현한 Bean을 만들어줌
//    }

//    @Bean
//    ErApi builderErApi(WebClient.Builder builder) {
//        WebClient webClient = builder.baseUrl(Domain.ORIGIN).build(); // 클라이언트를 좀 더 세부적으로 만들기 위해서는 builder 사용
//        WebClientAdapter adapter = WebClientAdapter.create(webClient);
//        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
//                .builderFor(adapter)
//                .build();
//
//        return httpServiceProxyFactory.createClient(ErApi.class);
//    }

    @Bean
    ErApi restClientErApi() {
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
                .builderFor(adapter)
                .build();

        return httpServiceProxyFactory.createClient(ErApi.class);
    }
}