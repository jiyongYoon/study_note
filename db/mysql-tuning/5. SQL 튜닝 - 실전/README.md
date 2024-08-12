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
