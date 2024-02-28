# Redis

---

## 1. Redis란

- Remote Dictionary Server
  - Remote: 각각의 서버 안에 Local로 존재하지 않고 다수의 서버가 사용할 수 있는 원격에 있다는 의미
  - Dictionary: Key-Value로 이루어진 HashTable 형태로 상수의 시간복잡도로 접근이 가능하다는 의미
- In-Memory Data
  - 백업을 제외한 모든 데이터를 RAM에 저장함

## 2. Redis의 특징

### 기본 특징
- **In-Memory**
  - 모든 데이터를 RAM에 저장 (백업, 스냅샷 제외)
- **Single Threaded**
  - 단일 thread에서 모든 task 처리
- **Cluster Mode**
  - 다중 노드에 데이터를 분산 저장하여 안정성 & 고가용성 제공
- **Persistence**
  - RDB(Redis Database) + AOF(Append only file)을 통해 영속성 옵션 제공
- **Pub/Sub**
  - Pub/Sub 패턴을 지원하여 손쉬운 어플리케이션 개발(채팅, 알림 등)

### 장점
- 높은 성능
- 필요한 Data Type 지원
- 클라이언트 라이브러리 존재
- 다양한 레퍼런스

### 사용 사례
- **Caching**
  - 임시 비밀번호
  - 로그인 세션
- **Rate Limiter**
  - 특정 API에 대한 요청 횟수를 제한하기 위한 기술
  - Fixed-Window 
  - Sliding-Window Rate Limiter(비율 계산기)
- **Message Broker**
  - 메시지 큐
- **실시간 분석 / 계산**
  - 순위표(Rank, Leaderboard)
  - 반경 탐색(Geofencing)
  - 방문자 수 계산(Visitors Count)
- **실시간 채팅**
  - Pub/Sub 패턴

## 3. Redis의 영속성

- Redis는 주로 캐시로 사용되지만 서비스의 지속성과 지연 방지를 위해 데이터 영속성 옵션을 제공함
- 아래 두가지 옵션이 있으며, 두가지 옵션 모두 동시적용도 가능함

### RDB(Redis Database)
  - Point-in-time Snapshot -> 재난 복구 또는 복제에 주로 사용
  - 스냅샷 시점으로 인해 일부 데이터의 유실 가능성이 있음
  - 스냅샷 생성 중 Redis 성능저하 발생
### AOF(Append Only File)
  - Redis에 적용되는 Write 작업 모두 log로 저장
  - 데이터 유실 위험이 적음
  - 재난 복구시 Write 작업을 위해 모든 log를 사용해야하기 때문에 RDB 옵션보다 복구 속도가 느림

## 4. Caching

- 데이터를 빠르게 읽고 처리하기 위해 데이터를 임시로 저장하는 기술
- 캐시: 캐싱된 데이터를 저장하는 임시 저장소

### 사용예시
- **CPU 캐시**
  - L1, L2, L3 캐시를 사용하여 CPU와 RAM 속도차이 보완
- **웹 브라우저 캐싱**
  - 클라이언트의 로컬 저장소에 데이터를 저장하여 재방문시 사용
- **DNS 캐싱**
  - 이전에 조회한 도메인 이름과 해당하는 IP 주소를 저장하여 재요청시 사용
- **데이터베이스 캐싱**
  - 버퍼풀
- **CDN**
  - 원본 서버의 컨텐츠를 PoP 서버에 저장하여 사용자와 가까운 서버에서 요청 처리
- **어플리케이션 캐싱**

### 캐시 패턴

- Cache-Aside pattern
- Cache-Through pattern
- Write-Behind pattern
- 기타 등등
- [자세한 설명 링크](https://blog.naver.com/sqlpro/222461191564)

---

## 5. Redis CLI 명령어

<img src="https://github.com/jiyongYoon/study_note/assets/98104603/5fc1efae-2c92-40bb-8377-b75d8974782c" alt="adder" width="40%" />

- 대소문자 구분은 없음
- redis cli 연결 `redis-cli`
- 데이터 저장 `SET {key} {value}`
- 데이터 조회 `GET {key}` (값이 없는 경우 `nil` 반환)
- 데이터 삭제 `DEL {key}` (삭제한 데이터 갯수를 리턴)
- 종료 `exit`

---

## 6. 데이터 타입

### String
- 문자열, 숫자, 직렬화 객체(JSON) 등을 저장할 수 있음.
  - `SET {key} {value}`
  - `SET book '{"price": 10000, "name": "책이름"}'` 
  - `MSET [{key} {value}]`
  - `MGET [{key}]`
        
     <img src="https://github.com/jiyongYoon/study_note/assets/98104603/958ab43f-0653-4d5a-83d5-7b47dc435d1b" alt="adder" width="40%" />
      
- Redis에서는 따로 숫자형 타입은 없음. 다만, 숫자형으로 캐스팅이 가능한 String을 저장하는 경우, 연산이 가능해짐.
  - `INCR {key}` - 값을 1 올림
  - `INCRBY {key} {value}` - 값을 value만큼 올림
- 보통 Key를 만들 때, 계층형 구조를 표현하기 위해서는 `:`을 컨벤션으로 사용한다.
  - `SET com:example:name jiyong`

### List
- String을 Doubly Linked List로 저장 (양방향 연결) -> push / pop에 최적화( O(1) )
- Queue(FIFO) / Stack(FILO) 구현에 사용됨
  - Redis에서 해당 자료구조를 직접 제공하는게 아니라, List를 활용해 사용자가 직접 Queue와 Stack처럼 사용한다는 뜻임
    
    <img src="https://github.com/jiyongYoon/study_note/assets/98104603/a3f30a43-c207-4220-81ab-008b2209fc3d" alt="adder" width="40%" />

    - Queue는 Push한 쪽이랑 반대로 POP
    - Stack은 Push한 쪽이랑 같은 방향으로 POP
    - 왼쪽부터 인덱스를 셀 때는 0, 1, 2...
    - 오른쪽부터 인덱스를 셀 때는 -1, -2, 
    - 리스트 전체 조회를 원한다면 `LRANGE {key} 0 -1` -> 왼쪽 첫번째부터 오른쪽 첫번째까지
    - 리스트 데이터 남기기 `LTRIM {index} {index}` -> 해당 인덱스의 데이터만 남기고 나머지 삭제

### Sets
- Unique string 을 저장하는 정렬되지 않은 집합
- Set Operation 사용 가능
  - intersection, union, difference
  
  <img src="https://github.com/jiyongYoon/study_note/assets/98104603/959b4772-3d07-4aae-836a-0fd4cd2d34df" alt="adder" width="60%" />

### Hashes
- field-value 구조를 갖는 데이터 타입
- 다양한 속성을 갖는 객체의 데이터 저장시 유용
  
  <img src="https://github.com/jiyongYoon/study_note/assets/98104603/18eb2c32-2eca-46aa-97da-9fb26674939c" alt="adder" width="60%" />
  <img src="https://github.com/jiyongYoon/study_note/assets/98104603/d329833a-e667-403c-a282-f63d8f165192" alt="adder" width="60%" />

### Sorted Set
- Sets + score 를 통해 정렬된 집합(추가로 score 속성이 생성됨)
- 내부적으로 Skip List + Hash Table로 이루어져 있음
- score가 동일하면 사전 편찬 순(일반적인 오름차순)으로 정렬됨

  <img src="https://github.com/jiyongYoon/study_note/assets/98104603/170ba6ac-c154-4662-b89d-61597c3a4f13" alt="adder" width="60%" />
  <img src="https://github.com/jiyongYoon/study_note/assets/98104603/ac1326bf-eff0-4f16-b8e5-6064048f7d41" alt="adder" width="60%" />
  <img src="https://github.com/jiyongYoon/study_note/assets/98104603/1f0eed31-9043-415e-ba6b-7f1dba9efabc" alt="adder" width="40%" />

### Stream
- append-only log에 consumer groups과 같은 기능을 더한 자료 구조
  - append-only log: 데이터가 항상 추가만 되는 구조를 가진 log
- 추가기능
  - Unique ID
    - 이벤트가 추가될 때, 해당 이벤트나 메시지는 unique id를 갖게 되며, unique id를 통해 하나의 entry를 읽을 때, O(1) 시간 복잡도를 갖는다.
  - Consumer Group
    - 분산 시스템에서 다수의 consumer가 event를 중복 없이 처리할 수 있도록 함
- 명령어
  - `XADD {key} *(unique id 자동 할당) action {action name} [{key} {value}]`
  
    <img src="https://github.com/jiyongYoon/study_note/assets/98104603/e5804024-a08b-406e-a6d7-ccabda3b3d8f" alt="adder" width="60%" />
    <img src="https://github.com/jiyongYoon/study_note/assets/98104603/6166b7c6-df22-4986-beb1-63034484fa38" alt="adder" width="60%" />
  
### Geospatials
- 좌표를 저장하고 검색하는 데이터 타입
- 거리 계산, 범위 탐색 등 지원
    
  <img src="https://github.com/jiyongYoon/study_note/assets/98104603/6a375b64-c952-4d4e-8f27-5edb14290b58" alt="adder" width="60%" />
  <img src="https://github.com/jiyongYoon/study_note/assets/98104603/3e33538b-fd58-4272-be4e-e74c13f03913" alt="adder" width="40%" />

### Bitmaps
- 실제 데이터 타입은 아니고, String에 binary operation을 적용한 인터페이스
- 최대 42억개의 binary 데이터 표현이 가능(2^32)

  <img src="https://github.com/jiyongYoon/study_note/assets/98104603/c90f033f-2b9d-4e10-b5c6-8950917a9b2e" alt="adder" width="60%" />
  
  - 날짜 별 log-in 을 체크하고 싶은 경우, 123번 유저는 1/1, 1/2 로그인하였고 456번 유저는 1/1만 로그인하였다고 가정하고 명령어 실습
  - 비트 연산은 `BITOP` 으로 가능하며, 연산결과는 다른 key값에 저장이 됨

  <img src="https://github.com/jiyongYoon/study_note/assets/98104603/66e2626f-82bb-4e8b-b810-c413d5f8148e" alt="adder" width="40%" />
  
  - 이 안에 실제 데이터는 bit 결과값이라 binary 값이 들어있음
  
    <img src="https://github.com/jiyongYoon/study_note/assets/98104603/fea05914-9f4d-4a5d-b442-5b708fbc6c7d" alt="adder" width="60%" />

### HyperLogLog
- 집합의 cardinality를 추정할 수 있는 확률형 자료구조
- 결과값이 실제와 다소 오차가 있을 수 있음을 내포함
  - 평균 에러 0.81%이며, 정확성 일부를 포기하고 저장공간을 매우 효율적으로 사용함
  - 해시값으로 count를 하기 때문에 해시 충돌이 일어나는 경우 오차가 발생할 수 있음
  - 즉, 실제 값을 저장하지 않음
    
    <img src="https://github.com/jiyongYoon/study_note/assets/98104603/f15274d5-6519-4407-8d14-565d7c133018" alt="adder" width="60%" />
    <img src="https://github.com/jiyongYoon/study_note/assets/98104603/c837f81f-18ba-4c06-aa85-f0fac4f73bd5" alt="adder" width="60%" />
    
    - 바이너리로 저장된 모습

### BloomFilter
- element가 집합 안에 포함되었는지 확인할 수 있는 확률형 자료구조(membership test)
- 결과값이 실제와 다소 오차가 있을 수 있음을 내포함
  - 역시 정확성 일부를 포기하고 저장공간을 매우 효율적으로 사용함
  - 특히 `false positive` 즉, 실제로 포함되지 않은 member를 포함되었다고 잘못 예측하는 경우가 발생함
  - 해시값을 2개 만들어서 표시하며, 검증하고자 하는 데이터가 들어오면 해시값을 만들어 2개가 있으면 포함, 없으면 포함하지 않는다고 판단하는 원리로 동작함
  - 때문에 역시 해시충돌이 발생하면 실제로 포함되지 않았는데 포함이 되었다고 잘못 예측하는 경우가 발생할 수 있음
- 해당 자료구조는 다른 모듈이 추가되어야 함(redis/redis-stack-server 이미지를 활용하면 BloomFilter를 사용할 수 있음)
  `docker run -p 6379:6379 -d --rm redis/redis-stack-server`

  <img src="https://github.com/jiyongYoon/study_note/assets/98104603/c7898ac3-4aaa-44ba-91a4-564e5aad405a" alt="adder" width="60%" />

---

## 7. Redis의 특수 명령어들

### 데이터 만료
- 특정시간 이후 만료시키는 기능. TTL(Time To Live)
- 초단위로 기록함
- 만료된 데이터는 더이상 조회되지는 않음
- 저장공간에서 바로 삭제하지는 않고 만료료 표시 후 백그라운드에서 주기적으로 삭제함
- **명령어**
  - 생성1: `SET {key} {value}` 이후 `EXPIRE {key} {second}`
  - 생성2: `SETEX {key} {second} {value}`
  - 조회: `TTL {key}` -> 만료 이전에는 `남은 초` / 만료 이후에는 `-2` / TTL 설정 없는경우 `-1`
  
    <img src="https://github.com/jiyongYoon/study_note/assets/98104603/93f512d8-2c48-4cb9-8ead-43f9f4c15597" alt="adder" width="60%" />
    <img src="https://github.com/jiyongYoon/study_note/assets/98104603/74ed02dc-01f4-4f14-94ca-e6f5bc51b1b0" alt="adder" width="60%" />
  
### SET NX/XX
- NX: 해당 Key가 존재하지 않는 경우에만 SET
- XX: 해당 Key가 이미 존재하는 경우에만 SET
- Null Reply: SET이 동작하지 않는 경우 (nil) 응답
    
  <img src="https://github.com/jiyongYoon/study_note/assets/98104603/84203d6f-fd4b-46e5-bf13-a40405743c73" alt="adder" width="60%" />

### Pub/Sub
- Publisher와 Subscriber가 서로 알지 못해도 통신이 가능하도록 `decoupling`된 패턴
- Publisher는 Channel에 메시지를 발행
- Subscriber는 Channel을 구독하여 해당 채널에 발행된 메시지를 수신
  
  <img src="https://github.com/jiyongYoon/study_note/assets/98104603/a64c1216-94c0-4623-a0f6-8ad4bcf69681" alt="adder" width="100%" />

### Pipeline
- 다수의 commands를 한 번에 요청하여 네트워크 성능을 향상시키는 기술
- Round-Trip Times 최소화
- 대부분의 클라이언트 라이브러리에서 지원

### Transaction
- 다수의 명령을 하나의 트랜잭션으로 처리하여 원자성을 보장하는 기술
- 중간에 에러가 발생하면 모든 작업 Rollback
- 하나의 트랜잭션이 처리되는 동안 다른 클라이언트의 요청이 중간에 끼어들 수 없음
- pipeline은 네트워크 퍼포먼스 향상을 위해 여러개의 명령어를 한 번에 요청하는 것이지만, Transaction은 원자성을 보장하기 위해 다수의 명령을 하나처럼 처리하는 기술임
- **명령어**
  - 트랜잭션 시작: `MULTI`
  - 트랜잭션 커밋: `EXEC`
  - 트랜잭션 롤백: `DISCARD`
  
  <img src="https://github.com/jiyongYoon/study_note/assets/98104603/7ee72a19-b7f8-4e21-8677-8ada6e94f20e" alt="adder" width="30%" />
