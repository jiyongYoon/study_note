package com.example.springboot.httpclient;

import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
interface ErApi {
    @GetExchange(Domain.PATH)
    Map getKRWRates();

    // 이런식으로 활용 가능
    @PostExchange(url = "/realms/{realm}/protocol/openid-connect/token")
    String getToken(
        @PathVariable String realm,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String jwt,
        @RequestHeader("Content-Type") String contentType,
        @RequestBody MultiValueMap<String, String> keycloakAccessTokenRequest);
    // RequestBody로 json 객체 당연히 가능하며, Content-type이 application/x-www-form-urlencoded 타입인 경우 MultiValueMap<String, String> 으로 넘겨주어야 파싱이 됨.
    // 아니면 RestClientConfig에서 MessageConverters를 적절히 추가해주어야 하는데, formdata 컨버터를 아직 찾지 못함..ㅋㅋ
}