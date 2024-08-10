# SQL 튜닝

---

## 예제 1

```sql
CREATE TABLE users (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(100),
                       department VARCHAR(100),
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

데이터 100만건을 임의로 넣어줌

### 최근 3일 이내에 생성된 유저 조회

```sql
SELECT * FROM users
WHERE created_at >= DATE_SUB(NOW(), INTERVAL 3 DAY);
```
약 250ms 정도가 평균적으로 걸렸다. 실행 계획을 확인해보자.

<img src="https://github.com/user-attachments/assets/0c5b841c-5e77-4e56-b420-abfb55b78768" alt="adder" width="100%" />

=> 정렬이 되어있으면 범위로 검색할 수 있기 때문에 **Index를 만들자**

```sql
CREATE INDEX idx_created_at ON users (created_at);

-- 재조회
SELECT * FROM users
WHERE created_at >= DATE_SUB(NOW(), INTERVAL 3 DAY);
```
평균 약 40ms로 단축되었다. 실행 계획을 확인해보자.

<img src="https://github.com/user-attachments/assets/8653dcdc-4978-470c-89a7-fb90c3582488" alt="adder" width="100%" />

`range` 검색으로 바뀌었고, 접근한 데이터 모두를 반환(`filtered = 100`)하는 것도 확인되었다!

### 최근 3일 이내 + 부서 조건 추가

```sql
SELECT * FROM users
WHERE department = 'Sales'
  AND created_at >= DATE_SUB(NOW(), INTERVAL 3 DAY);
```
평균 220ms 정도가 나왔다. 실행계획을 보자.

<img src="https://github.com/user-attachments/assets/318ab9bb-f124-4dce-839d-69eb901750bd" alt="adder" width="80%" />

**=> `created_at`이 범위검색이니 지난번처럼 이 컬럼에만 Index를 생성해보자.**

```sql
CREATE INDEX idx_created_at ON users (created_at);
```
평균 35ms가 나왔으며 실행계획은 아래와 같다.

<img src="https://github.com/user-attachments/assets/df027d8d-2ac6-43fe-b099-45ab5bcd4807" alt="adder" width="80%" />

`range` 스캔으로 바뀌면서 시간도 단축되고 더 효율적으로 변경되었다. 그러나 아직도 filtered의 비율이 10%에 불과하여 리턴되는 값에 비해 접근하는 데이터가 많은 편이다.

**=> 같은 조건이라면 순서를 바꾸어 먼저 Index를 사용한 범위검색을 한 후에 두번째 조건으로 부서 값을 비교하도록 하면 더 좋지 않을까?**

```sql
SELECT * FROM users
WHERE created_at >= DATE_SUB(NOW(), INTERVAL 3 DAY)
  AND department = 'Sales';
```

평균 36ms, 실행 계획도 동일하다. (예상과는 다르네...)

<img src="https://github.com/user-attachments/assets/df027d8d-2ac6-43fe-b099-45ab5bcd4807" alt="adder" width="80%" />

**=> 두 곳에 모두 인덱스를 만들면 부서 조건도 더 빨리 찾을 수 있을 것이다.**

```sql
CREATE INDEX idx_department_created_at ON users (department, created_at);
```

평균 27ms가 나왔으며, 예상했던 대로 부서 조건에 대한 접근도 매우 빠르게 했다.

<img src="https://github.com/user-attachments/assets/9a2452a6-ce9a-4971-9642-58f2e3d4367b" alt="adder" width="100%" />

**=> 부서에만 인덱스를 걸면 어떻게 될까?**

```sql
CREATE INDEX idx_department ON users (department);
```

평균 160ms 정도의 속도가 나왔으며, 실행계획은 아래와 같다.

<img src="https://github.com/user-attachments/assets/e670d294-d311-4016-ae8e-eee6bffe1eb3" alt="adder" width="80%" />

`type`이 ref다. 비고유인덱스를 사용했다는 의미이다.

**=> 부서랑 생성일시에 각각 인덱스를 걸면 어떻게 될까?**

```sql
CREATE INDEX idx_department ON users (department);
CREATE INDEX idx_created_at ON users (created_at);
```

평균 35ms 정도의 속도가 나왔으며, 실행계획은 아래와 같다.

<img src="https://github.com/user-attachments/assets/8b018d54-825b-4d2b-9aba-4c37215471ff" alt="adder" width="100%" />

`possible_keys`로 두 가지 인덱스가 후보였음을 확인했으나, 실제 사용된 index는 `idx_created_at` 한가지였음을 알 수 있다.
이로써 위 쿼리를 위해서는 굳이 두 가지를 따로 걸어 줄 필요는 없음을 알게 되었다. 

단, `멀티컬럼 인덱스`는 소폭 추가적인 성능 향상의 의미가 있었다. 그러나 유의미한 향상인지는 따져봐야한다. 
*인덱스는 반드시 `생성, 수정, 삭제` 작업에는 성능저하를 초래하기 때문이다.*


### 세부 실행계획을 알아보자

세부 실행계획을 다시 살펴보자.

1) 인덱스 X + (부서 & 생성일자) 조건

    ```sql
    -> Filter: ((users.department = 'Sales') and (users.created_at >= <cache>((now() - interval 3 day))))  (cost=93877 rows=33224) (actual time=0.604..225 rows=89 loops=1)
        -> Table scan on users  (cost=93877 rows=996810) (actual time=0.0815..173 rows=1e+6 loops=1)
    ```
    
    먼저 Full Table 스캔으로 10^6 개의 데이터에 접근 후 조건에 해당하는 값 89개를 필터링 해왔다.


2) `created_at` 인덱스 + (부서 & 생성일자) 조건

    ```sql
    -> Filter: (users.department = 'Sales')  (cost=477 rows=106) (actual time=0.154..2.98 rows=89 loops=1)
        -> Index range scan on users using idx_created_at over ('2024-08-07 22:24:29' <= created_at), with index condition: (users.created_at >= <cache>((now() - interval 3 day)))  (cost=477 rows=1060) (actual time=0.0466..2.92 rows=1060 loops=1)
    ```
    
    Index Range 스캔으로 created_at 조건에 해당하는 데이터 1060개를 먼저 가져온 후, 부서 조건에 맞는 89개의 데이터를 필터링했다.


3) `created_at` 인덱스 + (생성일자 & 부서) 조건

    ```sql
    -> Filter: (users.department = 'Sales')  (cost=477 rows=106) (actual time=0.105..2.89 rows=89 loops=1)
        -> Index range scan on users using idx_created_at over ('2024-08-07 22:27:18' <= created_at), with index condition: (users.created_at >= <cache>((now() - interval 3 day)))  (cost=477 rows=1059) (actual time=0.0248..2.84 rows=1059 loops=1)
   ```
   
    Where 조건 절의 순서가 바뀌어도, 2번과 동일한 순서로 데이터를 가져왔다. `옵티마이저`가 이를 효율적으로 변경한 것 같다.


4) `created_at & department` 인덱스 + (부서 & 생성일자) 조건

    ```sql
    -> Index range scan on users using idx_department_created_at over (department = 'Sales' AND '2024-08-07 22:29:38' <= created_at), with index condition: ((users.department = 'Sales') and (users.created_at >= <cache>((now() - interval 3 day))))  (cost=48.9 rows=108) (actual time=0.0319..0.398 rows=108 loops=1)
    ```
   
    Index Range 스캔으로 두 가지 index condition이 한번에 검색되었다.


5) `department` 인덱스 + (부서 & 생성일자) 조건

    ```sql
    -> Filter: (users.created_at >= <cache>((now() - interval 3 day)))  (cost=8900 rows=63765) (actual time=0.673..128 rows=100 loops=1)
        -> Index lookup on users using idx_department (department='Sales')  (cost=8900 rows=191314) (actual time=0.541..118 rows=100000 loops=1)
    ```

    Index를 사용해서 (lookup) 데이터를 100000개 읽은 후, 필터링을 진행했다.

6) `created_at`, `department` 인덱스 + (부서 & 생성일자) 조건

    ```sql
    -> Filter: (users.department = 'Sales')  (cost=473 rows=202) (actual time=0.176..3.38 rows=100 loops=1)
        -> Index range scan on users using idx_created_at over ('2024-08-07 22:59:42' <= created_at), with index condition: (users.created_at >= <cache>((now() - interval 3 day)))  (cost=473 rows=1050) (actual time=0.0256..3.33 rows=1050 loops=1)
    ```
   
    당연히 created_at 인덱스만 사용한 것과 결과는 동일하다.

---

## 예제 2

### 인덱스가 예상대로 사용되지 않는 경우 1

```sql
CREATE INDEX idx_name ON users (name);

EXPLAIN SELECT * FROM users
        ORDER BY name DESC;
```

위 경우는 인덱스를 사용하지 않고 Full Table Scan을 진행한다.
name으로 인덱스가 생성되어 있기는 하지만, 어차피 리턴해야하는 데이터는 테이블 전체 데이터이다.
따라서 옵티마이저는 인덱스를 거쳤다가 가기보다는 직접 접근하는 것이 효율적이라고 판단한 것이다.

아래처럼 sql이 바뀌면 옵티마이저는 인덱스를 사용하기도 한다.
```sql
EXPLAIN SELECT * FROM users
        ORDER BY name DESC
        LIMIT 1000;
```

<img src="https://github.com/user-attachments/assets/37513a56-9cb0-48d0-b115-3f8fbafcc3a6" alt="adder" width="100%" />

### 인덱스가 예상대로 사용되지 않는 경우 2

```sql
CREATE INDEX idx_name ON users (name);
CREATE INDEX idx_salary ON users (salary);
```

```sql
# User000000으로 시작하는 이름을 가진 유저 조회
EXPLAIN SELECT * FROM users
        WHERE SUBSTRING(name, 1, 10) = 'User000000';
```

<img src="https://github.com/user-attachments/assets/19e8e064-1018-4de3-8738-d4e7636228fb" alt="adder" width="80%" />

```sql
EXPLAIN SELECT * FROM users
        WHERE salary * 2 < 1000
        ORDER BY salary;
```

<img src="https://github.com/user-attachments/assets/0e3143d6-cda4-4a52-9776-c7b9922334bd" alt="adder" width="80%" />

두 경우 모두 인덱스가 걸려있는 컬럼을 `가공(substring, *2 등의 연산)`했기 때문에 인덱스를 사용하지 못하는 경우가 많다.
인덱스가 사용 가능하면서 의미가 동일하도록 튜닝해보자.

```sql
EXPLAIN SELECT * FROM users
        WHERE name LIKE 'User000000%';
```

<img src="https://github.com/user-attachments/assets/c77e83cc-5ac1-4672-829c-80f2d0db2273" alt="adder" width="80%" />

```sql
EXPLAIN SELECT * FROM users
        WHERE salary < 1000 / 2
        ORDER BY salary;
```

<img src="https://github.com/user-attachments/assets/450fc4e0-8f3d-484a-8c4a-f1fd2d8c5e2c" alt="adder" width="80%" />
