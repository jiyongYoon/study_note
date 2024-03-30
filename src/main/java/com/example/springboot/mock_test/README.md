# JUnit의 Mockito와 Test

개발을 할 때, 간단한 단위 기능부터 긴 플로우까지 테스트를 하고 싶은 경우가 있다.

레이어드 아키텍처를 기준으로, DB와 연동하여 두 가지 Layer 이상의 동작을 확인하고 싶은 경우(예를 들면, Service <-> Repository) `@SpringBootTest` 어노테이션을 활용하여 테스트를 진행하곤 했다.

## 테스트에 사용할 클래스들

### HelloService.java
```java
@Service
@RequiredArgsConstructor
public class HelloService {

    private final HelloRepository helloRepository;
    private final HelloClient helloClient;


    public String save() {
        System.out.println("HelloService save()");

        String data = helloClient.getData();
        String saveData = helloRepository.save(data);
        System.out.println("HelloService saveData = " + saveData);

        return saveData;
    }

}
```

### HelloClient.java

```java
@Component
public class HelloClient {

    public String getData() {
        String data = "HelloClient 실제 객체";
        System.out.println("HelloClient getData(), data = " + data);
        return data;
    }

}
```

### HelloRepository.java

```java
@Repository
public class HelloRepository {

    public String save(String data) {
        System.out.println("HelloRepository save(), data = " + data);
        return data;
    }

    public String findById(String data) {
        System.out.println("HelloRepository findById(), data = " + data);
        return data;
    }
}
```

## `@SpringBootTest`

간단하게, 스프링 컨테이너를 띄워서 테스트 코드를 실행하는 것이다. 때문에, 스프링에서 제공하는 기능들을 테스트 환경에서도 편하게 사용할 수 있다. 
컨테이너에 있는 Bean을 바로 주입받아 사용할 수 있으며, 스프링 AOP를 사용하여 처리하는 기능들(ex, 트랜잭션 등)을 쉽게 사용할 수 있다. 다만, 스프링 컨테이너를 띄우는 것이다보니 상대적으로 시간이 많이 걸리게 된다.

### HelloServiceAutowiredTest.java
```java
@SpringBootTest
public class HelloServiceAutowiredTest {

    @Autowired
    HelloService helloService;

    @Test
    void autowiredTest() {
        String data = helloService.save();

        Assertions.assertThat(data).isEqualTo("HelloClient 실제 객체");
    }
}
```

### console
```text
HelloService save()
HelloClient getData(), data = HelloClient 실제 객체
HelloRepository save(), data = HelloClient 실제 객체
HelloService saveData = HelloClient 실제 객체
```
- SpringBootTest이기 때문에, `@Autowired`로 빈을 주입받아서 바로 사용할 수 있다. 별 특이할 것이 없다.



---

## @Mock (모조품)

대상은 클래스(객체)이며, Mockito 라이브러리를 사용하여 클래스의 모조품을 만들어서 사용할 수 있다.

`HelloClient` 객체가 외부 통신을 하는 객체라고 한다면, 굳이 외부 객체 통신까지 테스트 할 필요가 없을수도, 혹은 테스트를 하기 어려울수도 있을 것이다. 이 때, Mock 객체를 만들어 사용할 수 있다.

### 패키지: `package org.mockito;`

패키지 명에서 알 수 있듯이, 이는 Spring과는 상관없는 라이브러리다.

### HelloServiceMockTest.java
```java
@ExtendWith(MockitoExtension.class)
public class HelloServiceMockTest {

    @Mock
    HelloClient helloClient;

    @Mock
    HelloRepository helloRepository;

    @InjectMocks
    HelloService helloService;

    @Test
    void mockTest() {
        // given
        String clientMockData = "HelloClient Mock 데이터";
        String repositoryMockData = "HelloRepository Mock 데이터";
        BDDMockito.given(helloClient.getData())
            .willReturn(clientMockData);
        BDDMockito.given(helloRepository.save(any()))
            .willReturn(repositoryMockData);

        // when
        String data = helloService.save();

        // then
        Assertions.assertThat(data).isEqualTo(repositoryMockData);
    }
}
```

### console

```text
HelloService save()
HelloService saveData = HelloRepository Mock 데이터
```

- 대용할 객체를 `@Mock` 어노테이션을 사용하여 Mock 객체로 만든다. 이러면 이 객체는 속이 빈 깡통으로 객체 형태만을 가지게 된다.
- 대신, 실제 동작할 메서드의 동작을 우리가 정의할 수 있다.
    - 코드에서는 `Mockito` 보다 좀 더 가독성이 좋은 `BDDMockito` 클래스를 사용하여 동작을 정의하였다.
- 정의된 메서드를 통해 실제 객체 메서드는 호출되지 않고, 개발자가 정의해둔 규칙으로 동작을 하게 된다.
- 콘솔 결과를 보면 실제 HelloClient나 HelloRepository의 메서드들이 호출되면 찍혀야 할 print 구문이 없는 것을 알 수 있다.

> `@SpringBootTest` 어노테이션을 사용하지는 않았지만, 함께 사용도 가능하다.
> 다른 `Bean`들을 주입받아 사용하고 싶으면 `@SpringBootTest` 어노테이션을 붙인 뒤 나머지 객체들은 여전히 `@Autowired`로 주입받아 사용하면 된다.
> 
> 그런데... 꼭 `@SpringBootTest` + `@Autowired` 조합만 가능할까? 
> 
> **내가 원하는 몇 가지 객체만, 혹은 몇 가지 메서드만 동작을 설정할 수는 없을까???**

---

## 골라서 주입하고 싶다.

사실 이번 학습의 주된 목표였다.
모든 메서드를 Mock으로 주입하여 동작을 정의하기에는 너무 귀찮았다...

`HelloService` 의 `HelloClient`는 가짜로, `HelloRepository` 객체는 실제 객체를 사용하고 싶었다. 그러나 `@InjectMocks` 어노테이션을 붙인 객체에 주입할 방법이 없었다.

> `@InjectMocks` 어노테이션의 설명 중
>
> Again, note that @InjectMocks will only inject mocks/spies created using the @Spy or @Mock annotation.
> `@Mock` 클래스랑 `@Spy` 클래스만 inject 한다...

---

## @Spy 

`@Spy` 어노테이션은, 실제 객체를 한번 더 감싸는 Mock 객체를 만드는데, 만약 개발자가 특정 동작을 정의하지 않는다면 실제 객체의 메서드를 사용하는 방식으로 동작한다.

### HelloServiceSpyTest.java

```java
@ExtendWith(MockitoExtension.class)
public class HelloServiceSpyTest {

    @Spy
    HelloRepository helloRepository;

    @Mock
    HelloClient helloClient;

    @InjectMocks
    HelloService helloService;

    @Test
    void spyTest() {
        // given
        String clientMockData = "HelloClient Mock 데이터";
        String methodData = "HelloRepository findById 데이터";
        BDDMockito.given(helloClient.getData())
            .willReturn(clientMockData);

        // when
        String saveData = helloService.save();

        String findData = helloRepository.findById(methodData);

        // then
        Assertions.assertThat(saveData).isEqualTo(clientMockData);
        Assertions.assertThat(findData).isEqualTo(methodData);
    }
}
```

### console

```text
HelloService save()
HelloRepository save(), data = HelloClient Mock 데이터
HelloService saveData = HelloClient Mock 데이터
HelloRepository findById(), data = HelloRepository findById 데이터
```

- `HelloClient`는 외부 서버 연동이라고 가정한다면, 테스트 범위 밖이 될 수 있다. 따라서 Mock 객체로 동작을 정의하였다.
- `HelloRepository`의 `save()` 메서드는 given-willReturn으로 정의해주었고, `findById()` 메서드는 따로 정의하지 않았다.
- **결과는 `save()` 메서드는 정의한대로, `findById()`는 실제 객체 메서드 그대로 동작하였다.**

---

## @MockBean, @SpyBean

`Bean`이 들어간 것을 보면 알 수 있듯이, 이는 springframework의 어노테이션이다. 

`package org.springframework.boot.test.mock.mockito.MockBean;`

@MockBean은 Mock객체를 스프링 컨테이너에 주입해주는 것이고,
@SpyBean은 Spy객체를 스프링 컨테이너에 주입해주는 것이다.

> 즉, `@SpringBootTest` + `@Autowired` 조합의 객체에서 사용하는 빈을 `MockBean` 또는 `SpyBean`의 형태로 주입하게 하는 것이다.

### 주의사항 1. `@Autowired`와 `@MockBean`의 우선순위

`@Autowired`와 `@MockBean`을 붙인 동일한 클래스가 필드에 두번 존재할 때, 객체는 어떻게 될까?

> `@MockBean` 어노테이션은 기존 스프링 컨테이너에 있는 클래스를 `대체`하기 때문에, 해당 클래스는 `MockBean`으로 대체된다.

### 주의사항 2. `@SpyBean`과 `Interface`

가장 흔하게 주입된 인터페이스를 사용하는 경우는 Spring Data JPA 객체일 것이다.
@SpyBean은 실제 구현된 객체를 감싸는 프록시 형태이기 때문에, 스프링 컨텍스트에서 구현체가 있어야 감쌀 수 있다. 따라서 **동일한 타입의 실제 구현체가 있어야 한다.**