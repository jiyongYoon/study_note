CQRS(Command Query Responsibility Segregation) 패턴

---

# 1. 핵심 개념

> `쓰기(Command)`와 `읽기(Query)`를 `분리`한다.
> 
> - 쓰기: 상태변경(insert/update/delete)
> - 읽기: 조회(read-only)

# 2. 왜 나누나?

1. 책임 분리: 쓰기와 읽기를 다른 책임으로 바라봄
2. 트래픽이 많은 경우 성능 최적화
   - 쓰기: 트랜잭션, 정합성 집중
   - 읽기: 캐시, read replica, denormalization

# 3. SpringBoot에서 사용하는 구조 예시

## 1) Service Layer

- 네이밍 컨벤션 사용
- `ReservationCommandService`: 예약관련 insert/update/delete
  ```java
  @Transactional
  @Service
  public class ReservationCommandService {
    public void createReservation(...) { ... }
  }
  ```
- `ReservationQueryService`: 예약관련 read
  ```java
  @Transactional(readOnly = true)
  @Service
  public class ReservationQueryService {
    public Reservation getReservation(...) { ... }
  }
  ```
  
> ### `readOnly = true`는 왜 조회 성능을 빠르게 할까?
> 
> **"이번 트랜잭션은 데이터 변경은 신경쓰지 않아도 돼. 그러니 관련 작업은 건너뛰어도 돼"라는 신호이다.**
> 
> JPA 사용 시, 일반적인 `@Transactional`은 영속성 컨텍스트를 생성한 후, 엔티티 조회 시 스냅샷을 저장한다.
> 이 스냅샷은 트랜잭션이 끝날 때 변경 감지(dirty checking)를 통해 객체관점으로 엔티티 변경상태를 DB에 반영해주는 역할을 한다.
> 때문에 데이터 변경을 위해서는 `flush`가 반드시 일어나야 한다.
> 
> 반면, `readOnly = true` 옵션을 주면 영속성 컨텍스트가 생성되지만, 
> dirty checking을 하지 않는다는 선언이기 때문에 해당 과정을 생략하고, 따라서 `flush`도 일어나지 않는다.
> 
> 이 과정에서 스냅샷을 보관하고 비교하는 과정을 생략하기 때문에 필요한 메모리와 CPU가 절약된다는 것이다.
> 
> > **readOnly 트랜잭션은 JPA 레벨에서는 dirty checking과 flush를 생략하도록 힌트를 주고, DB 벤더(mysql, mariaDB > postgresql)에 따라 read-only 트랜잭션으로 전달되어 추가적인 최적화(`START TRANSACTION READ ONLY`)가 적용될 수 있다**
    
## 2) Repository Layer

1. 쓰기
   - 하나의 Service에서만 처리함. 즉, `ReservationCommandService` -> `ReservationRepository`
   - 다른 도메인은 다른 Service에서 호출해야 함
2. 읽기
   - 다른 도메인 Repository도 자유롭게 사용함. join/조회 최적화 목적이 있기 때문에 도메인 순수성보다는 성능 우선

## 3) Read Replica 적용 시에는 어떻게 처리하나?

write는 master DB, read는 replica DB로 라우팅하도록 DataSource를 분리하고, 트랜잭션(readOnly 여부)에 따라 동적으로 선택한다.

### 전체 구조

```text
[ Repository ]
      ↓
[ EntityManager ]
      ↓
[ DataSource ]
      ↓
[ Routing DataSource ]
   ↙           ↘
Master DB     Replica DB
(write)        (read)
```

### 핵심 기술: AbstractRoutingDataSource

```java
public class RoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return TransactionSynchronizationManager.isCurrentTransactionReadOnly()
                ? "replica"
                : "master";
    }
}
```
- 현재 트랜잭션이 readOnly인지 체크하여 그에 맞는 DB 선택

### DataSource 구성

```java
@Configuration
public class DataSourceConfig {

  @Bean
  @ConfigurationProperties(prefix = "spring.datasource.master")
  public DataSource masterDataSource() {
    return DataSourceBuilder.create().build();
  }

  @Bean
  @ConfigurationProperties(prefix = "spring.datasource.replica")
  public DataSource replicaDataSource() {
    return DataSourceBuilder.create().build();
  }

  @Bean
  public DataSource routingDataSource() {
    RoutingDataSource routingDataSource = new RoutingDataSource();

    Map<Object, Object> dataSourceMap = new HashMap<>();
    dataSourceMap.put("master", masterDataSource());
    dataSourceMap.put("replica", replicaDataSource());

    routingDataSource.setTargetDataSources(dataSourceMap);
    routingDataSource.setDefaultTargetDataSource(masterDataSource());

    return routingDataSource;
  }
}
```

```java
// 트랜잭션 시작 전에 커넥션이 먼저 잡히는걸 방지해야 함. 이 설정이 없으면 항상 master로 가는 버그 발생함
@Bean
@Primary
public DataSource dataSource() {
    return new LazyConnectionDataSourceProxy(routingDataSource());
}
```

### EntityManager

- EntityManager는 하나만 사용하고, DataSource 레벨에서 분기가 됨

### application.yaml(PostgreSQL 예시)
```yaml
spring:
  datasource:
    master:
      jdbc-url: jdbc:postgresql://master-db:5432/mydb
      username: myuser
      password: mypassword
      driver-class-name: org.postgresql.Driver
      hikari:
        maximum-pool-size: 10

    replica:
      jdbc-url: jdbc:postgresql://replica-db:5432/mydb
      username: myuser
      password: mypassword
      driver-class-name: org.postgresql.Driver
      hikari:
        maximum-pool-size: 20
```

### 실무 상 중요한 포인트 - replication lag 지연 문제

replica 복사에서의 약간의 지연 문제로 인해 조회 시 데이터가 없는 것처럼 보일 수 있다.
해결 방법은 아래와 같다.
   1. 중요한 조회는 조회더라도 master로 강제 선택한다. `@Transactional(readOnly = false)`
   2. 데이터 변경이 이루어진 요청에서는 ThreadLocal 등을 활용해서 조회 트랜잭션도 master를 쓰도록 한다.