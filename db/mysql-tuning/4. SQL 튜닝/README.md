# SQL 튜닝

---

## 예제 1. Where 조건 SQL 튜닝

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

## 예제 2. 인덱스가 예상대로 사용되지 않는 경우

### 인덱스가 있어도 Full Table Scan으로 진행되는 경우

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

### 인덱스가 걸려있는 컬럼을 가공했을 경우

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

---

## 예제 3. Where과 Order by 가 동시에 있는 SQL 튜닝 

```sql
SELECT * FROM users
        WHERE created_at >= DATE_SUB(NOW(), INTERVAL 3 DAY)
          AND department = 'Sales'
        ORDER BY salary
        LIMIT 100;
```

### 1. 인덱스 없이 조회

- 시간: 230ms
- 스캔타입: ALL (Full Table Scan)
- Analyze
  ```text
   -> Limit: 100 row(s)  (cost=93924 rows=100) (actual time=224..224 rows=100 loops=1)
       -> Sort: users.salary, limit input to 100 row(s) per chunk  (cost=93924 rows=996636) (actual time=224..224 rows=100 loops=1)
           -> Filter: ((users.department = 'Sales') and (users.created_at >= <cache>((now() - interval 3 day))))  (cost=93924 rows=996636) (actual time=6.61..224 rows=104 loops=1)
               -> Table scan on users  (cost=93924 rows=996636) (actual time=0.0498..175 rows=1e+6 loops=1)
  ```
   먼저 Full Table Scan 후 조건에 맞는 데이터를 필터링, 그리고 정렬 조건에 맞게 정렬 후 상위 100개만 가져옴. 모든 작업을 그대로 진행함.

### 2. where 조건 (created_at) 인덱스 추가

```sql
CREATE INDEX idx_created_at ON users(created_at);
```

- 시간: 40ms
- 스캔타입: range (Inedex Range Scan)
- Analyze
   ```text
   -> Limit: 100 row(s)  (cost=490 rows=100) (actual time=3.19..3.2 rows=100 loops=1)
       -> Sort: users.salary, limit input to 100 row(s) per chunk  (cost=490 rows=1089) (actual time=3.19..3.2 rows=100 loops=1)
           -> Filter: (users.department = 'Sales')  (cost=490 rows=1089) (actual time=0.0997..3.15 rows=104 loops=1)
               -> Index range scan on users using idx_created_at over ('2024-08-08 00:10:52' <= created_at), with index condition: (users.created_at >= <cache>((now() - interval 3 day)))  (cost=490 rows=1089) (actual time=0.0233..3.1 rows=1089 loops=1)
   ```
   Index range sacn을 활용하여 created_at 조건에 맞는 데이터를 먼저 찾은 후, 나머지 Where 조건(department) 필터링을 하고, Order by 조건(salary)으로 정렬 후 상위 100개를 리턴함.

### 3. order by 조건 (salary) 인덱스 추가

```sql
CREATE INDEX idx_salary ON users(salary);
```

- 시간: 1676ms
- 스캔타입: index (Index Full Scan)
- Analyze
   ```text
   -> Limit: 100 row(s)  (cost=9.09 rows=3.33) (actual time=6.79..1380 rows=100 loops=1)
       -> Filter: ((users.department = 'Sales') and (users.created_at >= <cache>((now() - interval 3 day))))  (cost=9.09 rows=3.33) (actual time=6.79..1380 rows=100 loops=1)
           -> Index scan on users using idx_salary  (cost=9.09 rows=100) (actual time=0.543..1333 rows=977030 loops=1)
   ```
   Index Full Scan을 사용해 데이터에 접근 후, Where조건으로 필터링, 그리고 상위 100개를 리턴함.

### 4. 원인 분석

`order by` 조건에 Index를 걸었을 때, 분명 Index Full Scan을 사용했지만 그냥 Full Table Scan보다 성능이 더 안나왔다. 
Analyze를 보면 인덱스 스켄에 1333ms의 가장 긴 시간을 소요했는데, 그 이유는 `idx_salary` 테이블에는 `id`와 `salary` 값 밖에 없어서 필터링을 하기 위해 매번 실제 데이터가 있는 테이블에 접근을 해야하기 때문이다.
따라서 그냥 Full Scan보다 오버헤드가 많았던 것이다.

> 인덱스를 사용하여 SQL을 튜닝하는 경우, `최초 접근하는 데이터`의 양을 줄이는 것이 매우 중요하다.
> 그렇게 최초에 접근한 데이터의 양이 적으면 그 이후에는 어떤 작업을 해도 수월하기 때문이다!!

---

## 예제 4. Having 문이 사용된 SQL문 튜닝

```sql
SELECT age, MAX(salary) FROM users
    GROUP BY age
    HAVING age >= 20 AND age < 30;
```

### 1) 인덱스 없이 조회

- 시간: 243ms
- 스캔타입: ALL (Full Table Scan)
- Analyze
   ```text
   -> Filter: ((users.age >= 20) and (users.age < 30))  (actual time=236..236 rows=10 loops=1)
       -> Table scan on <temporary>  (actual time=236..236 rows=100 loops=1)
           -> Aggregate using temporary table  (actual time=236..236 rows=100 loops=1)
               -> Table scan on users  (cost=100624 rows=996389) (actual time=0.0542..133 rows=1e+6 loops=1)
   ```

### 2) age 인덱스 생성 후 조회

```sql
CREATE INDEX idx_age ON users (age);
```

- 시간: 1365ms
- 스캔타입: index (Index Full Scan)
- Analyze
   ```text
   -> Filter: ((users.age >= 20) and (users.age < 30))  (cost=200263 rows=101) (actual time=276..1346 rows=10 loops=1)
       -> Group aggregate: max(users.salary)  (cost=200263 rows=101) (actual time=17.8..1346 rows=100 loops=1)
           -> Index scan on users using idx_age  (cost=100624 rows=996389) (actual time=0.34..1316 rows=1e+6 loops=1)
   ```
  
   Index Full Scan 후 그룹핑을 먼저 하고, 그룹핑 된 데이터에서 필터링을 진행한다.
   idx_age 테이블만으로는 salary 값을 알 수 없기 때문에 `1316ms`의 시간을 사용해 실제 데이터 테이블까지 접근하여 salary 값을 가져왔기 때문에 오버헤드가 더 발생함.

### 3) age & salary 멀티 컬럼 인덱스 생성 후 조회

```sql
CREATE INDEX idx_age_salary ON users (age, salary);
```

- 시간: 30ms
- 스캔타입: range (Index Range Scan)
- Analyze
   ```text
   -> Filter: ((users.age >= 20) and (users.age < 30))  (cost=138 rows=102) (actual time=0.116..0.437 rows=10 loops=1)
       -> Covering index skip scan for grouping on users using idx_age_salary  (cost=138 rows=102) (actual time=0.0396..0.427 rows=100 loops=1)
   ```
   
   idx_age_salary 에는 리턴에 필요한 값이 모두 들어있다. 따라서 `커버링 인덱스`가 되므로 실제 데이터 테이블에 접근하지 않고 바로 데이터를 모두 가져올 수 있다.
   또, age가 1번, salary가 2번 인덱스이므로 age에서 범위 스켄이 가능하며, 범위 스캔을 하면서 바로 salary 값을 볼 수 있기 때문에 커버링이 가능하고, 성능이 빠른 것이다.

### 4) SQL 변경

2번에서 생성한 `age` 인덱스는 범위 스캔을 위해 필요해보인다. 그러나, MAX 값을 위해 `salary` 까지 멀티 컬럼 인덱스로 추가해야할까?
인덱스는 최소화 하는 것이 무조건 좋기 때문에, age 인덱스만 살려보자.
  
2번에서의 속도의 문제점은 처음 Index Full Scan 시 1) `모든 데이터에 접근`하기 때문에 최초 접근 데이터가 많았으며, 2) 모든 데이터의 `salary` 값까지 알아야 했기 때문에 오버헤드가 더 발생했다.
`GROUP BY`와 `HAVING ON`은 그룹핑을 한 후 Having 조건으로 데이터를 추린다. <br>
_**어차피 우리는 `age >= 20 AND age < 30`인 데이터만 필요하기 때문에, 최초 접근 조건에 이 내용을 반영하면 age 인덱스만으로 쿼리 성능을 대폭 향상 시킬 수 있을 것이다.**_

```sql
SELECT age, MAX(salary) FROM users
    WHERE age >= 20 AND age < 30
    GROUP BY age;
```

- 시간: 30ms
- 스캔타입: range (Index Range Scan)
- Analyze
```text
-> Group aggregate: max(users.salary)  (cost=103891 rows=101) (actual time=21.8..160 rows=10 loops=1)
    -> Index range scan on users using idx_age over (20 <= age < 30), with index condition: ((users.age >= 20) and (users.age < 30))  (cost=85002 rows=188892) (actual time=0.0251..156 rows=100214 loops=1)
```

Index Range Scan을 하게 되었다. 애초에 `20 <= age < 30` 이 범위에 해당하는 데이터만 Index를 활용해 범위 검색을 한 것이다.
이 후에 그룹핑을 하여 집계함수를 동작하면 최초 접근 데이터가 `996389` -> `188892`개로 줄어든 것을 볼 수 있다.

> GROUP BY 이후 처리되는 `HAVING` 에 있는 조건을 `WHERE` 절에 사용할 수 있는지 확인하자. <br>
> (HAVING을 쓸 수 밖에 없는 경우도 존재한다)