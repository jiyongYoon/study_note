package com.example.springboot.httpclient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    RestClient restClient(RestClient.Builder builder) {
        return builder
                .requestFactory(new JdkClientHttpRequestFactory())
                .baseUrl(Domain.ORIGIN)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//                .messageConverters(
//                    httpMessageConverters -> httpMessageConverters.add(new FormHttpMessageConverter())) // json 제외한 메시지들을 파싱하는 역할
                .build();
    }

    /* java.net.http.HttpClient 인터페이스 구현체들이 있다.
    JdkClientHttpRequestFactory: jdk에서 제공
    HttpComponentsClientHttpRequestFactory: 톰캣에서 제공
    JettyClientHttpRequestFactory: 제티에서 제공
    (ReactorNettyClientRequestFactory: 네티에서 제공, WebClient에 사용됨)
     */
}
