package com.example.springboot.httpclient;

import org.springframework.web.service.annotation.GetExchange;

import java.util.Map;

interface ErApi {
    @GetExchange("/v6/latest")
    Map getKRWRates();
}