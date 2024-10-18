package com.example.springboot.httpclient;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ClientTest implements ApplicationRunner {

    private final ErApi erApiBean;

    private final String domain = "https://open.er-api.com";
    private final String uri = "/v6/latest";

    @Override
    public void run(ApplicationArguments args) throws Exception {

        // RestTemplate
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Map<String, Double>> response = restTemplate.getForObject(domain + uri, Map.class);
        System.out.println("RestTemplate: " + response.get("rates").get("KRW"));

        // Webflux - WebClient
        WebClient webClient = WebClient.create(domain);
        Mono<Map> mapMono = webClient.get().uri(uri).retrieve().bodyToMono(Map.class);
        Map<String, Map<String, Double>> response2 = mapMono.block();
        System.out.println("WebClient: " + response2.get("rates").get("KRW"));

        // Webflux - HttpInterface
        WebClientAdapter adapter = WebClientAdapter.create(webClient);
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
                .builderFor(adapter)
                .build();
        ErApi erApi = httpServiceProxyFactory.createClient(ErApi.class);

        Map<String, Map<String, Double>> response3 = erApi.getKRWRates();
        System.out.println("HttpInterface: " + response3.get("rates").get("KRW"));

        // Webflux - HttpInterface Bean
        Map<String, Map<String, Double>> response4 = erApiBean.getKRWRates();
        System.out.println("HttpInterfaceBean: " + response4.get("rates").get("KRW"));
    }
}
