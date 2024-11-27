해당 내용은 JOIN ON, WHERE, HAVING 조건절이 어떤 의미와 차이가 있는지 스터디한 내용이다.

사용한 DB는 MySQL 8.0 버전이다.

---

# 1. 테이블 및 데이터

```sql
SET SESSION cte_max_recursion_depth = 1000000; -- insert에 필요한 session 개수 확보

-- TABLE A --
DROP TABLE IF EXISTS A;
CREATE TABLE A (
    id INT PRIMARY KEY,
    value INT
);

INSERT INTO a (id, value)
WITH RECURSIVE cte (n) AS
    (
        SELECT 1
        UNION ALL
        SELECT n + 1 FROM cte WHERE n < 1000000 -- 데이터 개수
    )
SELECT n, FLOOR(1 + RAND() * 1000) FROM cte;

-- TABLE B --
DROP TABLE IF EXISTS B;
CREATE TABLE B (
    a_id INT,
    description TEXT
);

INSERT INTO b (a_id, description)
WITH RECURSIVE cte (n) AS
    (
        SELECT 1
        UNION ALL
        SELECT n + 1 FROM cte WHERE n < 1000000 -- 데이터 개수
    )
SELECT
    n,
    CASE WHEN RAND() < 0.5 THEN 'apple' ELSE 'banana' END
FROM cte;
```

# 2. SELECT 쿼리 및 실행계획

```sql
-- 1 --
EXPLAIN ANALYZE
SELECT A.id, A.value, B.description
FROM A
JOIN B
    ON A.id = B.a_id AND B.description = 'apple';

-- 2 --
EXPLAIN ANALYZE
SELECT A.id, A.value, B.description
FROM A
JOIN B
    ON A.id = B.a_id
WHERE B.description = 'apple';
```

```sql
-- 1, 2 실행계획 --
-> Nested loop inner join  (cost=135573 rows=99823) (actual time=0.0387..4557 rows=500236 loops=1)
    -> Filter: ((b.`description` = 'apple') and (b.a_id is not null))  (cost=100635 rows=99823) (actual time=0.0267..2111 rows=500236 loops=1)
        -> Table scan on B  (cost=100635 rows=998230) (actual time=0.0242..1502 rows=1e+6 loops=1)
    -> Single-row index lookup on A using PRIMARY (id=b.a_id)  (cost=0.25 rows=1) (actual time=0.00448..0.00453 rows=1 loops=500236)
```

- 실행 계획이 동일하다.
    - 먼저 B 테이블을 스캔한다.
    - B 테이블의 조건으로 필터링을 한다. (ON 절이든 WHERE 절이든 여기서 진행됨)
    - Single-row index lookup (인덱스를 사용하여 단일 행만을 조회하는 행동)
        - b.a_id와 a.id가 같은 값을 찾기 위해 단 하나의 행 조회하기 위해 동작
    - 이 후 Nested loop로 inner join 진행

# 3. 원인

Inner Join의 경우, A와 B 테이블이 Join 된 후, 특정 조건에 맞는 데이터만 반환하면 된다. 
따라서 DB엔진은 ON 절에 추가된 조건이든, WHERE 절에 추가된 조건이든 `필터링의 대상`은 동일하게 `전체 조인 결과`이다.
때문에 실행계획이 동일하다.

# 4. 그렇다면 ON절과 WHERE 절이 왜 있는가

Inner Join이 아니라 Outer Join 시에는 의미가 있다.

## 예시: Left (Outer) Join

### (1) ON절

```sql
EXPLAIN ANALYZE
SELECT A.id, A.value, B.description
FROM A
LEFT JOIN B
    ON A.id = B.a_id AND B.description = 'apple';
```

```sql
-> Left hash join (b.a_id = a.id)  (cost=99.7e+9 rows=997e+9) (actual time=2440..4296 rows=1e+6 loops=1)
    -> Table scan on A  (cost=100314 rows=998568) (actual time=1.46..746 rows=1e+6 loops=1)
    -> Hash
        -> Filter: (b.`description` = 'apple')  (cost=0.129 rows=998230) (actual time=8.91..1905 rows=500236 loops=1)
            -> Table scan on B  (cost=0.129 rows=998230) (actual time=8.59..1542 rows=1e+6 loops=1)
```

- B 테이블을 먼저 스캔한다.
- On 절에 있는 조건으로 필터링한다.
- A 테이블을 스캔한다.
- Left Hash Join을 진행한다.
    - Left Outer Join 이기 때문에 A 테이블에 있는 모든 데이터를 다 불러오며, 그 중 B 테이블과 id값이 같은 데이터를 join한다.

⇒ A 테이블의 전체 데이터 + B 테이블의 description이 ‘apple’인 데이터만 추가로 붙임

### (2) WHERE 절

```sql
EXPLAIN ANALYZE
SELECT A.id, A.value, B.description
FROM A
LEFT JOIN B
    ON A.id = B.a_id
WHERE B.description = 'apple';
```

```sql
-> Nested loop inner join  (cost=135573 rows=99823) (actual time=2.19..4403 rows=500236 loops=1)
    -> Filter: ((b.`description` = 'apple') and (b.a_id is not null))  (cost=100635 rows=99823) (actual time=0.0407..1876 rows=500236 loops=1)
        -> Table scan on B  (cost=100635 rows=998230) (actual time=0.0366..1377 rows=1e+6 loops=1)
    -> Single-row index lookup on A using PRIMARY (id=b.a_id)  (cost=0.25 rows=1) (actual time=0.00467..0.00472 rows=1 loops=500236)
```

- 실행 계획이 기존과 동일하다.

⇒ A 테이블의 전체 데이터 + B 테이블의 전체 데이터 join 후 description이 ‘apple’인 데이터만 필터링

# 5. 조건 위치를 옮김으로 성능이 개선되는 상황

그룹함수의 조건을 주는 Having 절의 조건을 Join 시 On 절로 조건을 옮기는게 좋다.
Having 절은 그룹함수 동작 후에 동작해야 하기 때문에 모수 데이터를 적게 해주는 것이 성능상 유리하다.

> **sql 실행 순서**
> 
> `FROM` -> `WHERE` -> `GROUP BY` -> `HAVING` -> `SELECT` -> `ORDER BY`

즉, 그룹함수 조건이 아닌 기본 조건은 모수를 줄이기 위해 WHERE 절로 이동하는 것이 좋다는 것이다.