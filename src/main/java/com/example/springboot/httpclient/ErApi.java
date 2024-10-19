package com.example.springboot.httpclient;

import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.Map;

@HttpExchange
interface ErApi {
    @GetExchange(Domain.PATH)
    Map getKRWRates();
}