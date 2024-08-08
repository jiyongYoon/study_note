# 실행계획

## 실행계획이란

- 옵티마이저가 SQL문을 어떤 방식으로 어떻게 처리할 지를 계획한 내용.
- 이 내용을 보고 사용자가 튜닝을 할 근거를 찾게 된다.

## 실행계획 확인 방법

```sql
-- 실행 계획 조회하기
EXPLAIN [SQL문]

-- 실행 계획에 대한 자세한 정보 조회하기
EXPLAIN ANALYZE [SQL문]
```

### 예제

```sql
EXPLAIN
SELECT * FROM users
WHERE age = 23;
```

<img src="https://github.com/user-attachments/assets/c7f679ba-35c0-4b99-9616-aeb5efbd58a1" alt="adder" width="100%" />

> SQL 튜닝에 필수적인 컬럼부터 익히고, 세부적인 내용은 나중에 추가로 학습하자!

1. `id` - 실행 순서
2. `table` - 조회한 테이블 명
3. `type` *중요* - 테이블 데이터를 어떤 방식으로 조회했는지
4. `possible_keys` / `key`- 여러 인덱스 종류 중 / 이 인덱스를 사용함
5. `ref` - 어떤 값을 기준으로 Join 작업을 했는지
6. `rows`(추청치) *중요* - sql 수행을 위해 테이블에 접근한 갯수 (access count) -> 이 값을 줄이는 것이 SQL 튜닝의 핵심!
7. `filtered`(추청치) - 응답 값 / 불러온 값. 즉, 접근을 얼마나 효율적으로 했는지 확인할 수 있는 비율
8. `Extra` - 부가정보

```sql
EXPLAIN ANALYZE
SELECT * FROM users
WHERE age = 23;
```

<img src="https://github.com/user-attachments/assets/b898d855-9d91-41d9-a500-c15774e3d225" alt="adder" width="70%" />

- 깊은 계층부터 읽어나가면 됨.

**1. 1번 작업**
- `Table scan on users` - users 테이블을 Full scan 함.
  - `actual time=0.0273..0.0309` - 실제로 걸린 시간(ms) (첫번째 데이터에 접근한 시간)..(모든 데이터에 접근 마무리한 시간)
  - `rows=7` - 접근한 데이터 수

**2. 2번 작업**
- `Filter: (users.age = 23)` - 가져온 데이터를 (조건)으로 필터링함.
  - `actual time=0.0291..0.0325` - 실제로 걸린 시간(ms) ..(해당 작업을 마무리하기까지 걸린 전체 누적시간)
    - 즉, Full Scan을 한 후 (0.0309ms) 필터링을 함(0.0325 - 0.0309 = 0.0016ms)
  - `rows=2` - 접근한 데이터 수

## 실행계획의 `type`의 의미 해석

- 성능 개선 가능성을 `*`의 갯수로 표현

### ALL**

- 풀 테이블 스캔 (Full Table Scan)
- 인덱스를 활용하지 않고 테이블을 처음부터 끝까지 전부 다 뒤져서 데이터를 찾음

### Index*

- 풀 인덱스 스캔 (Full Index Scan)
- 인덱스 테이블을 처음부터 끝까지 전부 다 뒤져서 데이터를 찾음
- 데이터 테이블 전체를 읽는 것보다는 효율적이지만, 역시 인덱스 테이블 전체를 읽기 때문에 아주 효율적이라고 할 수 없음

### Const

- 1 건의 데이터를 바로 찾을 수 있는 경우 -> 아주 효율적임
- 고유 인덱스(Unique Index) 또는 기본 키(PK)를 사용해서 조회한 경우 출력됨
  - 고유 인덱스와 기본 키는 모두 `해당 데이터는 무조건 1건`이라는 것을 보장하기 때문에 인덱스에서 해당 값을 찾으면 바로 리턴할 수 있는 것임

### Range*

- 인덱스 레인지 스캔 (Index Range Scan)
- 인덱스를 활용해 범위 형태의 데이터를 조회한 경우
  - `BETWEEN`, `부등호`, `IN`, `LIKE`를 활용한 데이터 조회를 뜻함
- 기본적으로 인덱스를 사용하여 효율적이긴 하지만, 데이터를 조회하는 범위가 클 경우 성능 저하의 원인이 될 수 있음

### Ref

- 비고유 인덱스를 사용한 경우(Uniqui가 아닌 인덱스), 데이터가 고유하지는 않더라도 정렬은 되어 있음

### eq_ref, index_merge, ref_or_null...

- 위에 언급된 자주 나오는 타입 먼저 익숙해진 후 공부하자