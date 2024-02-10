# Index

학습 영상 링크: [유튜브 쉬운코드](https://youtu.be/IMDH4iAQ6zM?si=tVdqnL83bw22Mb0R)

---

## Index

- 색인
- 조건을 만족하는 튜플(데이터)을 빠르게 조회(`select`)하기 위해서 사용한다!
- 빠르게 정렬(`order by`)하거나 그룹핑(`group by`)을 하기 위해서 사용되기도 한다.

```sql
SELECT *
FROM customer
WHERE first_name = 'Minsoo';
```

- first_name에 index가 없다면?
  - `full scan(=table scan)`으로 모든 데이터에 접근하여 조건절에 맞는지 검사를 한다.
  - 시간 복잡도: O(N)
- first_name에 index(B-tree)가 있다면?
  - 시간 복잡도: O(logN)

### 사용될 예시

- 예시 테이블 구조

    <img src="https://github.com/jiyongYoon/study_cs_note/assets/98104603/cf6965db-f184-40f8-b56a-5bf1cbb21012" alt="adder" width="60%" />

- 사용되는 sql문
    ```sql
    SELECT * FROM player WHERE name = 'Sonny';
    SELECT * FROM player WHERE team_id = 105 and backnumber = 7;
    ```
  
### index 생성문
- 테이블에 데이터가 있는 경우
  ```sql
  1. 중복이 가능한 단일 컬럼 index를 만드는 경우
  
  CREATE INDEX {인덱스명} ON {인덱스 적용 테이블명} ({인덱스를 만들 컬럼명});
  CREATE INDEX player_name_idx ON player (name);
  
  2. 팀 별로 등번호는 고유하게 식별이 되기 때문에, 멀티 컬럼 index를 만드는 경우
  
  CREATE UNIQUE INDEX {인덱스명} ON {인덱스 적용 테이블명} ({컬럼명1, 컬럼명2});
  CREATE UNIQUE INDEX team_id_backnumber_idx ON player (team_id, backnumber);
  ```
  
- 테이블을 만들 때 인덱스를 함께 만드는 경우
  ```sql
  CREATE TABLE player (
    id          INT             PRIMARY KEY, 
    name        VARCHAR(20)     NOT NULL,
    team_id     INT,
    backnumber  INT,
    INDEX player_name_idx (name),
    UNIQUE INDEX team_id_backnumber_idx (team_id, backnumber)
  );  // 인덱스 이름은 생략 가능
  ```

- Primary Key의 경우는 대부분의 RDB에서 pk값 index가 자동 생성된다.

### index 확인문

```sql
SHOW INDEX FROM player;
```

<img src="https://github.com/jiyongYoon/study_cs_note/assets/98104603/6d879c67-40ae-4730-95bd-49e2ff8d1bdb" alt="adder" width="60%" />

### index 동작 확인

```sql
EXPLAIN
SELECT * FROM player WHERE backnumber = 7;
```

### optimizer가 사용할 index 명시

```sql
SELECT * FROM player USE INDEX {인덱스명} WHERE backnumber = 7; -> 웬만하면 사용 유도
SELECT * FROM player FORCE INDEX {인덱스명} WHERE backnumber = 7; -> 강력하게 권고 
```

## B-tree 기반의 인덱스 동작 방식

- `CREATE INDEX index_a ON members (a);`

  <img src="https://github.com/jiyongYoon/study_cs_note/assets/98104603/78c42197-c1e6-436d-b87d-110f4e468528" alt="adder" width="60%" />

  - a에 대해 `정렬`이 되어 인덱스가 생성된다.
  - 실제 데이터의 주소값을 가지고 있으며, 주소값이 실제 데이터와 매핑된다.

- `WHERE a = 9`를 찾는다면?
  1. index 테이블에서 탐색을 통해 9와 동일한 값을 찾는다. 
  2. 실제 데이터에 접근하여 응답으로 만든다. 
  3. 이후 정렬의 특성을 이용하여 위 또는 아래 방향으로 스캔을 하여 같은 값인 경우를 스캔하며 2번 작업을 반복한다.

- `WHERE a = 9 and b = 10`을 찾는다면??
  1. index 테이블에서 탐색을 통해 9와 동일한 값을 찾는다. 
  2. 실제 데이터에 접근하여 b의 조건을 추가로 검토한다.
  3. 조건이 성립하면 응답으로 만든다.
  4. 이후 정렬의 특성을 이용하여 위 또는 아래 방향으로 스캔을 하여 같은 값인 경우를 스캔하며 2번 작업을 반복한다.
  
  => 즉, 첫 번째 조건인 a 값만 인덱스를 활용할 수 있고, b는 모든 데이터를 확인해보아야 한다. <br>
  => 이 상황을 개선하기 위해서는 멀티 컬럼 인덱스를 활용할 수 있다.

- `WHERE a = 9 and b = 10`을 `CREATE INDEX(a, b)`에서 찾는다면?? <br>
  => 인덱스의 순서는 `a` -> `b` 순으로 정렬이 된다.
  1. index 테이블에서 탐색을 통해 9와 동일한 값을 찾는다. 
  2. a 인덱스에서 b 인덱스를 또 탐색하여 동일하게 접근한다.
  3. 조건이 성립하면 실제 데이터에 접근하여 응답으로 만든다.

> 인덱스 짱이네? 다 만들면 안돼??

## index 사용 유의사항

1. index 마다 테이블이 생성되기 때문에, 데이터가 CUD 될 때마다 index도 변경 작업을 해야하기 때문에 성능 저하 발생
2. index를 위한 추가적인 저장 공간 차지

## Covering index

```sql
CREATE INDEX ON player (team_id, backnumber);

SELECT team_id, backnumber FROM player WHERE team_id = 5;
```

- SELECT 문에서 응답할 데이터가 이미 인덱스 안에 다 있기 때문에 실제 테이블에 갈 필요가 없어지는 상황.
  - 인덱스 스캔만으로 응답이 되니 조회성능이 더 빨라지기 때문에, 의도적으로 사용하기도 함.

## Hash index

- hash table을 사용하여 index를 구현함. 따라서 시간복잡도가 `O(1)`
- 장점
  - equality 조회 성능이 더 빠르다
- 단점
  - hash로 저장하기 때문에 Array 자료구조를 가지고 있게 될텐데, 이 때 이 자료구조의 범위를 넘어서면 더 큰 Array를 만들어야 하고, 이 때 발생하는 `rehashing` 작업에 대한 부담이 있다.
  - equality 조회를 제외한 조회에는 인덱스 사용이 불가능하다. (ex, 범위)
  - 멀티 컬럼 인덱스 (a, b)를 만들었을 때, 
    - B-Tree index는 a 조건 조회에도 사용이 가능한데, 
    - hash index는 a, b 모두를 사용하는 조건에서만 사용이 가능하다.

## Full scan이 더 좋은 경우

- 데이터가 매우 적을 경우 (몇 백건 정도..)
- 조회 대상이 전체 모수의 20% 이상일 경우