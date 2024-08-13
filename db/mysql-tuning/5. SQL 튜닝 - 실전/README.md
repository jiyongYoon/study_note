# SQL 튜닝 - 실전

---

## 유저 이름으로 특정 기간에 작성된 글 검색하는 SQL문 튜닝하기

```sql
CREATE TABLE users (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(50) NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE posts (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       title VARCHAR(255) NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       user_id INT,
                       FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### 개선 전

```sql
SELECT p.id, p.title, p.created_at
FROM posts p
         JOIN users u ON p.user_id = u.id
WHERE u.name = 'User0000046'
  AND p.created_at BETWEEN '2022-01-01' AND '2024-03-07';
```

- 시간: 177ms
- explain

  <img src="https://github.com/user-attachments/assets/2e79cb3c-b2fd-44ab-bd81-bc4d6a8678be" alt="adder" width="100%" />

- Analyze
  ```text
  -> Nested loop inner join  (cost=135345 rows=11084) (actual time=0.159..185 rows=1 loops=1)
      -> Filter: (u.`name` = 'User0000046')  (cost=100428 rows=99763) (actual time=0.0466..185 rows=1 loops=1)
          -> Table scan on u  (cost=100428 rows=997632) (actual time=0.0368..128 rows=1e+6 loops=1)
      -> Filter: (p.created_at between '2022-01-01' and '2024-03-07')  (cost=0.25 rows=0.111) (actual time=0.111..0.112 rows=1 loops=1)
          -> Index lookup on p using user_id (user_id=u.id)  (cost=0.25 rows=1) (actual time=0.0482..0.0992 rows=25 loops=1
  ```
  
- 해석 및 개선
  - user 테이블에서 풀 테이블 스캔을 진행한 후 u.name 조건에 맞는 값을 필터링하였고, user_id 인덱스를 사용하여 스캔을 진행하여 아이디 조건을 만족한 데이터를 찾은 후 생성일시 조건을 적용했다. 그리고 마지막 inner join을 진행했다.
  - user 테이블에서 풀 테이블 스캔하는데 모든 데이터에 접근했으니, 이 부분을 개선해주면 될 것이다. => **`u.name`에 인덱스를 걸자!**

### 개선 후

```sql
CREATE INDEX idx_user_name ON users(name);
```

- 시간: 30ms
- explain

  <img src="https://github.com/user-attachments/assets/c4cdcc04-86cd-49a6-9b4f-6a16bb68a35d" alt="adder" width="100%" />

- Analyze
  ```text
  -> Nested loop inner join  (cost=9.43 rows=2.29) (actual time=0.126..0.128 rows=1 loops=1)
      -> Covering index lookup on u using idx_user_name (name='User0000046')  (cost=1.1 rows=1) (actual time=0.0128..0.0133 rows=1 loops=1)
      -> Filter: (p.created_at between '2022-01-01' and '2024-03-07')  (cost=6.49 rows=2.29) (actual time=0.112..0.113 rows=1 loops=1)
          -> Index lookup on p using user_id (user_id=u.id)  (cost=6.49 rows=20.6) (actual time=0.0889..0.101 rows=25 loops=1)
  ```
  
- 해석
  - user_id pk 인덱스로 스캔하며 id 조건을 만족시킨 후, 생성일자 조건을 만족시키는 값을 필터링함. 이후 username 필터링을 진행했고 마지막 inner join을 하였다.
  - user 테이블에서 name 데이터를 접근해야 했는데, 인덱스 데이터만으로 가능한 `convering index` 조건이 발동되어 user 테이블에 접근하지 않음. 

---

## 특정 부서에서 전체 부서의 최대 연봉 값과 동일한 연봉을 가진 사람을 조회하기

```sql
CREATE TABLE users (
                     id INT AUTO_INCREMENT PRIMARY KEY,
                     name VARCHAR(100),
                     department VARCHAR(100),
                     salary INT,
                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

SELECT *
FROM users
WHERE salary = (SELECT MAX(salary) FROM users)
  AND department IN ('Sales', 'Marketing', 'IT');
```

### 개선 전

- 시간: 1299ms
- Explain

  <img src="https://github.com/user-attachments/assets/2743f034-1794-479d-891c-a92901e2df38" alt="adder" width="80%" />

- Analyze

  ```text
  -> Filter: ((users.salary = (select #2)) and (users.department in ('Sales','Marketing','IT')))  (cost=100569 rows=29899) (actual time=568..1209 rows=3 loops=1)
      -> Table scan on users  (cost=100569 rows=996636) (actual time=0.0737..639 rows=1e+6 loops=1)
      -> Select #2 (subquery in condition; run only once)
          -> Aggregate: max(users.salary)  (cost=200232 rows=1) (actual time=436..436 rows=1 loops=1)
              -> Table scan on users  (cost=100569 rows=996636) (actual time=0.0328..343 rows=1e+6 loops=1)
  ```
- 해석 및 개선
  - subquery를 먼저 실행
    - Full Table Scan을 하고, 거기서 Max 집계함수를 한 후, row 1개를 리턴한다.
  - 다시 Full Table Scan을 하고, 거기서 where 조건 2가지를 통해 데이터를 추린다.
  - where 조건에 salary 값이 비교조건에 1번, subquery의 max에 1번, 총 2번이 사용되기 때문에 salary에 인덱스를 걸어주면 max값과 조건절을 빠르게 통과할 수 있을 것으로 보인다.

### 개선 후

- 시간: 47ms
- Explain

  <img src="https://github.com/user-attachments/assets/517b55a0-1679-45b2-8113-557fd2bd6b32" alt="adder" width="80%" />

- Analyze
  ```text
  -> Filter: ((users.salary = (select #2)) and (users.department in ('Sales','Marketing','IT')))  (cost=2.24 rows=2.4) (actual time=0.0573..0.0618 rows=3 loops=1)
      -> Index lookup on users using idx_salary (salary=(select #2))  (cost=2.24 rows=8) (actual time=0.0541..0.0572 rows=8 loops=1)
      -> Select #2 (subquery in condition; run only once)
          -> Rows fetched before execution  (cost=0..0 rows=1) (actual time=400e-6..500e-6 rows=1 loops=1)
  ```
  
- 해석
  - subquery가 먼저 진행되는데, 정렬되어있는 salary에서 max값을 뽑는 작업이 매우 빨리 이루어짐
  - 이후 Index Full Scan을 진행하며, where 조건절에 해당하는 데이터를 추출함

---

## 부서별 최대 연봉을 가진 사용자들 조회

```sql
CREATE TABLE users (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(100),
                       department VARCHAR(100),
                       salary INT,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

SELECT u.id, u.name, u.department, u.salary, u.created_at
FROM users u
       JOIN (
  SELECT department, MAX(salary) AS max_salary
  FROM users
  GROUP BY department
) d ON u.department = d.department AND u.salary = d.max_salary;
```

### 개선 전

- 시간: 3408ms
- Explain

  <img src="https://github.com/user-attachments/assets/e5221945-2cea-49c4-b56c-8321fc412aa7" alt="adder" width="100%" />

- Analyze
  ```text
  -> Nested loop inner join  (cost=2.59e+6 rows=0) (actual time=1740..3860 rows=16 loops=1)
      -> Filter: ((u.department is not null) and (u.salary is not null))  (cost=100569 rows=996636) (actual time=0.0669..895 rows=1e+6 loops=1)
          -> Table scan on u  (cost=100569 rows=996636) (actual time=0.0659..708 rows=1e+6 loops=1)
      -> Covering index lookup on d using <auto_key0> (department=u.department, max_salary=u.salary)  (cost=0.25..2.5 rows=10) (actual time=0.00272..0.00272 rows=16e-6 loops=1e+6)
          -> Materialize  (cost=0..0 rows=0) (actual time=1582..1582 rows=10 loops=1)
              -> Table scan on <temporary>  (actual time=1582..1582 rows=10 loops=1)
                  -> Aggregate using temporary table  (actual time=1582..1582 rows=10 loops=1)
                      -> Table scan on users  (cost=100569 rows=996636) (actual time=0.0193..587 rows=1e+6 loops=1)
  ```
  
- 해석 및 개선
  - 조인에 필요한 베이스 테이블을 조회한다.
    - Table Full Scan으로 유저테이블에서 department와 salary가 null이 아닌 데이터만 전체 조회를 한다.
  - 조인할 테이블을 조회한다.
    - Table Full Scan으로 집계함수를 실행하고, 그 중 join 조건 2개로 필터링을 진행한다.
  - 조인한다.
  - department별 그룹핑을 하기 때문에, department로 인덱스가 생성되어 있으면 그룹핑 작업이 더 빨라질 것이다.
  - salary의 max값을 얻어야 하기 때문에 salary로 인덱스가 생성되어 있으면 max값을 얻는게 빨라질 것이다.

### 개선 1 - department만 인덱스 생성

```sql
CREATE INDEX idx_department ON users(department);
```

- 시간: 6872ms
- Explain

  <img src="https://github.com/user-attachments/assets/c4c9775e-ff25-499d-9a21-184766371fd7" alt="adder" width="100%" />

- Analyze
  ```text
  -> Nested loop inner join  (cost=3.49e+6 rows=8.97e+6) (actual time=4666..6853 rows=16 loops=1)
      -> Filter: ((u.department is not null) and (u.salary is not null))  (cost=100569 rows=996636) (actual time=0.0614..923 rows=1e+6 loops=1)
          -> Table scan on u  (cost=100569 rows=996636) (actual time=0.0604..719 rows=1e+6 loops=1)
      -> Covering index lookup on d using <auto_key0> (department=u.department, max_salary=u.salary)  (cost=200234..200236 rows=10) (actual time=0.00568..0.00568 rows=16e-6 loops=1e+6)
          -> Materialize  (cost=200233..200233 rows=9) (actual time=4492..4492 rows=10 loops=1)
              -> Group aggregate: max(users.salary)  (cost=200232 rows=9) (actual time=452..4491 rows=10 loops=1)
                  -> Index scan on users using idx_department  (cost=100569 rows=996636) (actual time=0.128..4145 rows=1e+6 loops=1)
  ```

- 해석
  - 개선은 커녕 더 오래걸리게 되었다. 실행계획을 보니, Index Full Scan으로 department 인덱스는 활용하지만, salary의 값을 얻기 위해 결국 메인 테이블로 가는 과정에서 오버헤드가 많이 발생하는 것으로 보인다.

### 개선 2 - salary만 인덱스 생성

- 시간: 1300ms
- Explain

  <img src="https://github.com/user-attachments/assets/a3aa220f-02c4-45ad-bf9d-c8e761035394" alt="adder" width="100%" />

- Analyze
  ```text
  -> Nested loop inner join  (cost=3.56e+6 rows=984775) (actual time=2033..2033 rows=16 loops=1)
      -> Filter: (d.max_salary is not null)  (cost=0.113..112124 rows=996636) (actual time=2032..2032 rows=10 loops=1)
          -> Table scan on d  (cost=2.5..2.5 rows=0) (actual time=2032..2032 rows=10 loops=1)
              -> Materialize  (cost=0..0 rows=0) (actual time=2032..2032 rows=10 loops=1)
                  -> Table scan on <temporary>  (actual time=2032..2032 rows=10 loops=1)
                      -> Aggregate using temporary table  (actual time=2032..2032 rows=10 loops=1)
                          -> Table scan on users  (cost=100569 rows=996636) (actual time=0.102..744 rows=1e+6 loops=1)
      -> Filter: (u.department = d.department)  (cost=2.47 rows=0.988) (actual time=0.0411..0.0482 rows=1.6 loops=10)
          -> Index lookup on u using idx_salary (salary=d.max_salary)  (cost=2.47 rows=9.88) (actual time=0.0369..0.0445 rows=10.6 loops=10)
  ```
- 해석
  - salary 인덱스를 사용하여 ON 조건을 처리하는 과정은 빨라졌다. 하지만, department로 그룹핑하고 max값을 찾아내는 과정은 역시나 Full Table Scan을 통해 모든 데이터에 접근하고 있다.

### 개선 3 - department, salary 각각 인덱스 생성

- 시간: 4451ms
- Explain

  <img src="https://github.com/user-attachments/assets/ae66b7f5-265a-421b-8141-b38528edc541" alt="adder" width="100%" />

- Analyze
  ```text
  -> Nested loop inner join  (cost=3.56e+6 rows=1.09e+6) (actual time=4830..4830 rows=16 loops=1)
      -> Filter: (d.max_salary is not null)  (cost=200233..112124 rows=996636) (actual time=4830..4830 rows=10 loops=1)
          -> Table scan on d  (cost=200234..200236 rows=9) (actual time=4830..4830 rows=10 loops=1)
              -> Materialize  (cost=200233..200233 rows=9) (actual time=4830..4830 rows=10 loops=1)
                  -> Group aggregate: max(users.salary)  (cost=200232 rows=9) (actual time=459..4829 rows=10 loops=1)
                      -> Index scan on users using idx_department  (cost=100569 rows=996636) (actual time=0.179..4447 rows=1e+6 loops=1)
      -> Filter: (u.department = d.department)  (cost=2.47 rows=1.1) (actual time=0.0377..0.0469 rows=1.6 loops=10)
          -> Index lookup on u using idx_salary (salary=d.max_salary)  (cost=2.47 rows=9.88) (actual time=0.0334..0.043 rows=10.6 loops=10)
  ```
- 해석
  - ON절 조건은 기존대로 salary 인덱스를 사용해 처리하고 있다.
  - 그룹핑을 위해 department에 인덱스를 주었는데, `개선 1` 상황과 동일한 오버헤드가 발생중이었다. (왜 생각을 못했을까)

### 개선 4 - department&salary 멀티컬럼 인덱스 생성

- 시간: 35ms
- Explain

  <img src="https://github.com/user-attachments/assets/7d08fcf0-4b8f-4863-8667-d3a3dc4ee624" alt="adder" width="100%" />

- Analyze
  ```text
  -> Nested loop inner join  (cost=9.6 rows=16.5) (actual time=0.214..0.305 rows=16 loops=1)
      -> Filter: ((d.department is not null) and (d.max_salary is not null))  (cost=14.3..3.62 rows=10) (actual time=0.189..0.191 rows=10 loops=1)
          -> Table scan on d  (cost=15.8..18.1 rows=10) (actual time=0.186..0.188 rows=10 loops=1)
              -> Materialize  (cost=15.5..15.5 rows=10) (actual time=0.185..0.185 rows=10 loops=1)
                  -> Covering index skip scan for grouping on users using idx_department_salary  (cost=14.5 rows=10) (actual time=0.0856..0.166 rows=10 loops=1)
      -> Index lookup on u using idx_department_salary (department=d.department, salary=d.max_salary)  (cost=0.449 rows=1.65) (actual time=0.0102..0.0111 rows=1.6 loops=10)
  ```
  
- 해석
  - 드디어 그룹핑 할 때 접근하는 데이터 수가 줄었다. 심지어 필요한 데이터가 인덱스 테이블에 모두 있어서 `Covering Index`가 적용되었다.
  - 압도적으로 성능이 좋아졌다. 다만, `멀티컬럼 인덱스`라는 점이 좀 아쉬웠다. (쓰기 작업에 더 큰 손실이 생기기 때문에...)

---

## 특정 년도의 주문데이터 조회하기

```sql
CREATE TABLE users (
                     id INT AUTO_INCREMENT PRIMARY KEY,
                     name VARCHAR(100),
                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE orders (
                      id INT AUTO_INCREMENT PRIMARY KEY,
                      ordered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      user_id INT,
                      FOREIGN KEY (user_id) REFERENCES users(id)
);

SELECT *
FROM orders
WHERE YEAR(ordered_at) = 2023
ORDER BY ordered_at
LIMIT 30;
```

### 개선 전

- 시간: 212ms
- Explain

  <img src="https://github.com/user-attachments/assets/a8cbfe16-e354-43ea-a9d0-3d321eebc808" alt="adder" width="100%" />

- Analyze

  ```text
  -> Limit: 30 row(s)  (cost=100334 rows=30) (actual time=264..264 rows=30 loops=1)
      -> Sort: orders.ordered_at, limit input to 30 row(s) per chunk  (cost=100334 rows=998285) (actual time=264..264 rows=30 loops=1)
          -> Filter: (year(orders.ordered_at) = 2023)  (cost=100334 rows=998285) (actual time=0.0336..256 rows=99792 loops=1)
              -> Table scan on orders  (cost=100334 rows=998285) (actual time=0.0284..126 rows=1e+6 loops=1)
  ```
  
- 해석 및 개선
  - Full Table Scan으로 전체 데이터를 돌면서 where 조건에 맞는 데이터 추출. 이 후 정렬 및 상위 30개 추림
  - 최초 접근 데이터를 줄이기 위해 ordered_at에 index를 걸면 좋으나, `YEAR(ordered_at) = 2023` 으로 컬럼이 가공되어 있어 인덱스 활용이 어려움
  - ordered_at에 인덱스를 주고 where조건을 범위조건으로 변경해주자

### 개선 후

```sql
CREATE INDEX idx_ordered_at on orders(ordered_at);

SELECT *
FROM orders
WHERE ordered_at >= '2023-01-01 00:00:00' AND ordered_at < '2024-01-01 00:00:00'
ORDER BY ordered_at
  LIMIT 30;
```

- 시간: 30ms
- Explain

  <img src="https://github.com/user-attachments/assets/74541d7d-ce5c-471b-aa91-6da040e14a74" alt="adder" width="100%" />

- Analyze

  ```text
  -> Limit: 30 row(s)  (cost=89575 rows=30) (actual time=0.0247..0.18 rows=30 loops=1)
    -> Index range scan on orders using idx_ordered_at over ('2023-01-01 00:00:00' <= ordered_at < '2024-01-01 00:00:00'), with index condition: ((orders.ordered_at >= TIMESTAMP'2023-01-01 00:00:00') and (orders.ordered_at < TIMESTAMP'2024-01-01 00:00:00'))  (cost=89575 rows=199056) (actual time=0.0242..0.179 rows=30 loops=1)
  ```
  
- 해석
  - 의도대로 Index range scan을 진행한다.

---

## 특정 기간의 성적이 x점인 학생을 조회하기

```sql
CREATE TABLE students (
                          student_id INT AUTO_INCREMENT PRIMARY KEY,
                          name VARCHAR(100),
                          age INT
);

CREATE TABLE subjects (
                          subject_id INT AUTO_INCREMENT PRIMARY KEY,
                          name VARCHAR(100)
);

CREATE TABLE scores (
                        score_id INT AUTO_INCREMENT PRIMARY KEY,
                        student_id INT,
                        subject_id INT,
                        year INT,
                        semester INT,
                        score INT,
                        FOREIGN KEY (student_id) REFERENCES students(student_id),
                        FOREIGN KEY (subject_id) REFERENCES subjects(subject_id)
);

SELECT
  st.student_id,
  st.name,
  AVG(sc.score) AS average_score
FROM
  students st
    JOIN
  scores sc ON st.student_id = sc.student_id
GROUP BY
  st.student_id,
  st.name,
  sc.year,
  sc.semester
HAVING
  AVG(sc.score) = 100
   AND sc.year = 2024
   AND sc.semester = 1;
```

- 시간: 7259ms
- Explain

  <img src="https://github.com/user-attachments/assets/83e8bc54-314c-4537-abfd-508f3451c619" alt="adder" width="100%" />

- Analyze

  ```text
  -> Filter: ((??? = 100) and `(sc.``year`` = 2024)` and `(sc.semester = 1)`)  (actual time=6872..7616 rows=1019 loops=1)
      -> Table scan on <temporary>  (actual time=6871..7531 rows=937406 loops=1)
          -> Aggregate using temporary table  (actual time=6870..6870 rows=937405 loops=1)
              -> Nested loop inner join  (cost=495151 rows=997458) (actual time=9.76..3680 rows=1e+6 loops=1)
                  -> Table scan on st  (cost=102589 rows=997458) (actual time=9.48..195 rows=1e+6 loops=1)
                  -> Index lookup on sc using student_id (student_id=st.student_id)  (cost=0.294 rows=1) (actual time=0.00317..0.00337 rows=1 loops=1e+6)
  ```
  
- 해석 및 개선
  - JOIN 시 조건 비교할 때 sc 테이블은 Index Full Scan을 진행했고, st 테이블은 Full Table Scan을 진행했다. 이후 조인하고 그룹핑 후 Having 조건절로 필터링했다.  
  - FROM - JOIN ON 절에서 JOIN 할 때 전체 데이터에 대해서 진행하는 부분에서 3600ms 정도나 걸렸다.
  - 그 후 그룹핑하는 쪽에서도 그룹핑할 데이터가 많다보니 6870 - 3680ms나 더 걸렸다.
  - SQL에서는 JOIN을 모든 데이터가 하지 않도록 WHERE 조건으로 먼저 st의 범위를 줄여주는게 좋겠다.
  - 인덱스는 1차 개선 후 다시 확인해보자.

### 개선 1 - HAVING 조건에서 이동 가능한 조건을 JOIN ON으로 이동

```sql
SELECT
    st.student_id,
    st.name,
    AVG(sc.score) AS average_score
FROM
    students st
        JOIN
    scores sc ON st.student_id = sc.student_id
        AND sc.year = 2024
        AND sc.semester = 1
GROUP BY
    st.student_id,
    st.name,
    sc.year,
    sc.semester
HAVING
       AVG(sc.score) = 100;
```

- 시간: 454ms
- Explain

  <img src="https://github.com/user-attachments/assets/57fbf18c-d29a-4c17-84a1-8b3cd3081e5f" alt="adder" width="100%" />

- Analyze
  ```text
  -> Filter: (??? = 100)  (actual time=1092..1106 rows=1019 loops=1)
      -> Table scan on <temporary>  (actual time=1092..1099 rows=94087 loops=1)
          -> Aggregate using temporary table  (actual time=1092..1092 rows=94087 loops=1)
              -> Nested loop inner join  (cost=104085 rows=9974) (actual time=10.1..986 rows=100348 loops=1)
                  -> Filter: ((sc.semester = 1) and (sc.`year` = 2024) and (sc.student_id is not null))  (cost=100477 rows=9974) (actual time=10.1..327 rows=100348 loops=1)
                      -> Table scan on sc  (cost=100477 rows=997442) (actual time=10.1..287 rows=1e+6 loops=1)
                  -> Single-row index lookup on st using PRIMARY (student_id=sc.student_id)  (cost=0.262 rows=1) (actual time=0.00642..0.00645 rows=1 loops=100348)
  ```

- 해석 및 개선
  - 필터링 해서 JOIN을 하기 때문에 전체 데이터가 아닌 일부 데이터를 JOIN하여 시간이 줄어들었다. 
  - 베이스 테이블인 students 테이블의 Full Table Scan은 막을 수 없는 것인가...? 이 데이터와 함께 JOIN 하는 부분이 시간이 가장 많이 소요된다.
  - JOIN 자체는 어쩔 수 없는 작업이라고 친다면, 무엇을 개선할 수 있을까? <br>
    -> 그룹핑하고 리턴하는 값 중, `student_id`는 이미 클러스터링 인덱스가 있고, `year`, `semester`는 JOIN 조건절에서 걸렀다. <br>
    -> score는 평균을 내기 때문에 굳이 정렬을 할 필요가 없는 데이터다. `name`만 정렬이 되어 있다면 그룹핑과 해당 인원의 점수의 평균을 내는 작업을 빨리 할 수 있을 것 같다! <br>
    => `name`에 인덱스를 걸어보자!

### 개선 2 - st.name 컬럼에 인덱스 적용

```sql
CREATE INDEX idx_st_name ON students(name);
```

- 시간: 460ms
- Explain

  <img src="https://github.com/user-attachments/assets/7e6d886d-5dda-48bc-9aba-28df65539eba" alt="adder" width="100%" />

- Analyze

```text
-> Filter: (??? = 100)  (actual time=830..843 rows=1019 loops=1)
    -> Table scan on <temporary>  (actual time=830..837 rows=94087 loops=1)
        -> Aggregate using temporary table  (actual time=830..830 rows=94087 loops=1)
            -> Nested loop inner join  (cost=106082 rows=9974) (actual time=0.0505..730 rows=100348 loops=1)
                -> Filter: ((sc.semester = 1) and (sc.`year` = 2024) and (sc.student_id is not null))  (cost=100473 rows=9974) (actual time=0.0376..211 rows=100348 loops=1)
                    -> Table scan on sc  (cost=100473 rows=997442) (actual time=0.0345..170 rows=1e+6 loops=1)
                -> Single-row index lookup on st using PRIMARY (student_id=sc.student_id)  (cost=0.462 rows=1) (actual time=0.00503..0.00506 rows=1 loops=100348)
```