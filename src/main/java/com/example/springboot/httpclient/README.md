# Spring 6 이상에서 사용가능한 Client 3종

- RestTemplate, WebClient, 그리고 HttpInterface

---

이 내용은 [토비의 스프링 유튜브 영상](https://youtu.be/Kb37Q5GCyZs?si=8J-xxYWyhOLaPoOY)을 보고 학습 및 실습한 내용을 정리한 것입니다.

---

## 1. RestTemplate (Web)

- 동기&블로킹 처리방식으로 많은 동시 요청을 처리해아하는 상황에서는 쓰레드 자원 낭비로 성능 문제가 생길 수 있다.

### 사용

- 호출
  ```java
  RestTemplate restTemplate = new RestTemplate();
  Map<String, Map<String, Double>> response = restTemplate.getForObject(domain + uri, Map.class);
  System.out.println("RestTemplate: " + response.get("rates").get("KRW"));
  ```

## 2. WebClient (Webflux)

- 비동기&논블로킹 처리방식을 지원하며 메서드 체이닝 등을 활용하여 비동기 작업을 쉽게 구현할 수 있다.

### 사용

- 의존성 추가
  ```groovy
  implementation 'org.springframework.boot:spring-boot-starter-webflux
  ```

- 호출
  ```java
  WebClient webClient = WebClient.create(domain);
  Mono<Map> mapMono = webClient.get().uri(uri).retrieve().bodyToMono(Map.class);
  Map<String, Map<String, Double>> response2 = mapMono.block(); // 여기서는 동기 방식으로 응답을 받아옴
  System.out.println("WebClient: " + response2.get("rates").get("KRW"));
  ```


## 3. HttpInterface (Webflux)
[공식문서 링크](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html#rest-http-interface)

- WebClient와 완전히 통합되며, Spring Framework 생태계에 통합되어 사용할 수 있다.
  - 따라서 WebClient가 가진 비동기&논블로킹 처리방식을 그대로 지원한다. (그러나 비동기와 반응형 프로그래밍을 주 목적으로 한 도구는 아니라고 한다.)
- 선언적인 API 클라이언트를 제공한다.
  - 인터페이스에 HTTP 요청 메서드를 선언하고, Spring이 이를 자동으로 구현해주는 방식이다.
  - 이렇게 하면 보일러 플레이트 코드를 줄여 생산성 향상에 도움을 줄 수 있다.

### 사용

- 의존성 추가
  ```groovy
  implementation 'org.springframework.boot:spring-boot-starter-webflux
  ```

- Interface(API) 정의
  ```java
  interface ErApi {
      @GetExchange("/v6/latest")
      Map getKRWRates();
  }
  ```

- Client Bean 등록
  ```java
  @Bean
  ErApi erApi() {
      WebClient webClient = WebClient.create(domain);
      WebClientAdapter adapter = WebClientAdapter.create(webClient);
      HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
              .builderFor(adapter)
              .build();

      return httpServiceProxyFactory.createClient(ErApi.class); // RestClient를 구현한 Bean을 만들어줌
  }
  ```
  
- 호출
  ```java
  // erApiBean을 DI로 주입받아 사용
  Map<String, Map<String, Double>> response4 = erApiBean.getKRWRates();
  System.out.println("HttpInterfaceBean: " + response4.get("rates").get("KRW"));
  ```

## 4. 동기와 비동기, 블로킹과 논블로킹, 반응형 프로그래밍

해당 내용을 학습하다보니 동기와 비동기, 블로킹과 논블로킹, 그리고 반응형 프로그래밍에 대해 추가로 학습하게 되었다.
([동기와 비동기, 블로킹과 논블로킹이 잘 설명되어 있는 블로그](https://velog.io/@nittre/%EB%B8%94%EB%A1%9C%ED%82%B9-Vs.-%EB%85%BC%EB%B8%94%EB%A1%9C%ED%82%B9-%EB%8F%99%EA%B8%B0-Vs.-%EB%B9%84%EB%8F%99%EA%B8%B0))

궁금증이 생겼다.

> 다량의 처리가 필요한 서버에서는 동기보다 비동기 방식이 효율적인 것은 알겠는데, 반응형 프로그래밍이 무엇이길래 비동기적 프로그래밍을 한 단계 더 발전시킨 개념이라고 하는걸까?

반응형 프로그래밍과 Spring WebFlux에 대한 기본개념을 잘 설명해놓은 (블로그 글)[https://jh-labs.tistory.com/776]이 있어서 읽어보았고, 비동기적인 방식을 넘어서 `제어의 역전`을 만들어 결합도를 낮추고 쓰레드 자원의 효율성을 높일 수 있다는 것을 알게 되었다.

정리를 해보자면
1. `비동기 프로그래밍`은 논블로킹 방식으로 쓰레드가 다른 작업을 할 수 있지만, 응답이 올 때마다 쓰레드가 새로 생성되거나 쓰레드풀이 관리되는 방식에서 비효율이 발생할 수 있다.
2. `반응형 프로그래밍`은 단일 이벤트 루프나 작은 쓰레드 풀을 사용해, 쓰레드를 효율적으로 관리하면서 배압 처리(소비자가 처리량 속도 조절)가 가능하다. 즉, 쓰레드와 응답 사이의 결합도를 낮추고 처리 과정을 유연하게 분리할 수 있다.
3. 처리 방식에서도 `콜백 지옥`이나 복잡한 비동기 흐름 대신 `체이닝`을 통해 코드의 일관성을 유지할 수 있고, 순차적인 `데이터 흐름`을 명확하게 표현할 수 있어 유지보수가 더 쉬워진다.

정도로 정리할 수 있겠다. (사실 아직 비동기, 반응형 프로그래밍 코드를 실제로 작성해서 사용해본적이 없어서 이론적으로 이해하고 있는 중이다...)

## 5. OkHttpClient + Retrofit2

기존에 결제 서버를 구현할 때, 해당 스펙으로 ApiClient를 사용한 적이 있다. API를 선언적으로 정의하기 때문에 가독성 및 유지보수에 좋다고 판단하여 적용하였다. HttpInterface와는 어떤 차이점이 있을까?

- Spring 프레임워크 통합
  - 가장 큰 차이점으로, Spring 프레임워크에서 통합하여 지원할 수 있는 여러 기능을 사용할 수 있음. (e.g. Spring AOP 등)
- 반응형 지원
  - Retrofit의 비동기 호출은 콜백 기반으로 기본적으로 반응형은 지원하지 않음.

따라서 사용목적이 다소 달라진다.

> **HttpInterface**
>
> - Spring 기반 프로젝트에서 선언적 API 클라이언트를 사용하고 싶을 때
> 
> **OkHttpClient + Retrofit**
> 
> - 독립적인 라이브러리로 안드로이드 개발이나 간단한 마이크로서비스에서 독립적인 REST 클라이언트로 사용하고 싶을 때

## 6. 정리

학습한 내용으로 나만의 정리를 해보았다. 경험치가 없어 이론적인 내용만을 반영했다.

1. 외부 서버에 간단하게 REST API call이 필요하다? -> `RestTemplate` 사용 (무척 간단)
2. 동일한 서버에 여러 REST API call을 많이 활용해야한다? -> `HttpInterface` 사용 (Client 설정도 범용적으로 가능)
3. 특정 REST API call을 세밀하게 컨트롤해야한다? -> `WebClient` 사용 (헤더, 쿠키, 인증처리, 오류 핸들링 등 시나리오 활용 가능)

---

### 추가 참고자료

- [Baeldung](https://www.baeldung.com/spring-6-http-interface#client-proxy)
- [요즘IT 블로그글](https://yozm.wishket.com/magazine/detail/1334/)