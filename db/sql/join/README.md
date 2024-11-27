# Join이란

두 개 이상의 table들에 있는 데이터를 한 번에 조회하는 것.

# Join syntax 종류

## Implicit Join (암묵적)

- from 절에는 table들만 나열하고 where 절에 join condition을 명시하는 방식
- old-style join syntax
- 가독성이 많이 떨어져서 실수가 발생할 가능성이 커진다.

### 에시 sql

```sql
SELECT D.name
FROM employee AS E, department AS D
WHERE E.id = 1 and E.dept_id = D.id;
```

## explicit join (명시적)

- 암묵적 join에서 발전한 join 문법
- from 절에 JOIN 키워드를 함께 사용

### 예시 sql

```sql
SELECT D.name
FROM employee AS E
         JOIN department AS D
              ON E.dept_id = D.id
WHERE E.id = 1;
```

# Join 형태 종류

## Inner Join

두 table에서 join condition을 만족하는 tuple들로 result table을 만드는 join

<img src="https://github.com/user-attachments/assets/79eba8e0-aa01-4bfb-905c-6c7411a5c138" alt="adder" width="40%" />

```sql
SELECT *
FROM employee AS E 
	(INNER) JOIN department AS D <- 생략 가능
		ON E.dept_id = D.id;
```

E.dept_id와 D.id가 같은 데이터가 있는 tuple들만 불러옴!

E.dept_id는 있는데 연관관계에 있는 D.id가 없다거나,

E.dept_id가 없는 경우는 결과에서 제외된다는 뜻이다.

- 깊게 들어가면, null은 = 연산 시 unknown이 되는데, 이 값은 true가 아니기 때문에 결과값에서 제외되는 것이다.

## Outer Join

<img src="https://github.com/user-attachments/assets/4f7f8624-032d-46fb-a611-d72716e10ecf" alt="adder" width="70%" />

```sql
FROM table1 LEFT [OUTER] JOIN table2 ON join_condition
FROM table1 RIGHT [OUTER] JOIN table2 ON join_condition
FROM table1 FULL [OUTER] JOIN table2 ON join_condition
```

- LEFT는 드라이빙 테이블, RIGHT는 조인 테이블이 기준 테이블이며, 기준 테이블은 모두 불러오는 것이 기본이다.
- FULL은 양쪽 테이블 데이터가 모두 불러와지며, Join이 가능한 데이터만 묶어준다.
    - FULL은 postgresql에서만 지원한다.

## Using

join을 할 때, join을 할 컬럼을 명시해주어 join하는 기능. 컬럼명이 동일해야 사용가능

```sql
SELECT *
FROM employee E JOIN department D USING (dept_id);
```

단, E.dept_id, D.id가 따로 나오지 않고 dept_id 컬럼으로 묶인 결과값이 나옴.

> MySQL에서는 cross join = inner join = join 이다.
cross join에 ON(or USING)을 같이 쓰면 inner join으로 동작한다.
inner join(or join)이 ON(or USING) 없이 사용되면 cross join으로 동작한다.
>

# DB 엔진의 동작 관점에서 Join의 종류

- Nested Loop Join(중첩 루프 조인)
- Hash Join(해시 조인)
- Sort Merge Join(정렬 병합 조인)

## Nested Loop Join

[[DB] 데이터베이스 NESTED LOOPS JOIN (중첩 루프 조인)에 대하여](https://coding-factory.tistory.com/756)

이중 for 문의 형태로 데이터를 찾아서 Join한다.

따라서 기준이 되는 드라이빙 테이블의 데이터가 많을 수록 Join 속도가 매우 느려진다.

### 성능 개선 포인트

- 드라이빙 테이블을 선택할 때 WHERE 절로 최대한의 데이터를 거르고 나서 Loop를 도는 것이 유리하다.
- Driven 테이블에 인덱스가 있으면 Full Table Scan을 하지 않아도 되므로 유리해진다. (인덱스는 결국 정렬의 의미이다.)

## Hash Join

[[DB] 데이터베이스 HASH JOIN (해시 조인)에 대하여](https://coding-factory.tistory.com/758)

드라이빙 테이블과 조인될 테이블의 조인 키 값을 해시 알고리즘으로 비교하여 매치되는 값을 Join하는 방식이다.

단, Hash 알고리즘으로 비교하기 때문에 `=` 비교 조인에서만 사용이 가능하다.

### 성능 개선 포인트

- 결국 Hash 작업을 얼마나 빨리 잘 하느냐가 관건이기 때문에, CPU 성능과 작업에 필요한 메모리 확보가 필요하다.
- Nested Loop 와 마찬가지로 드라이빙 테이블의 데이터 갯수를 줄이는 것이 중요하다.

## Sort Merge Join

[[DB] 데이터베이스 SORT MERGE JOIN (정렬 병합 조인)에 대하여](https://coding-factory.tistory.com/757)

양쪽 테이블을 먼저 정렬한 후 정렬한 결과를 차례로 Scan하며 Join 조건으로 Merge 해나가는 방식이다.

### 성능 개선 포인트

- 정렬 속도가 개선되면 성능이 개선 될 것이다. Join할 컬럼이 이미 정렬이 되어 있다면 (인덱스가 생성되어 있다면) 유리할 것이다.
- 양쪽 정렬이 모두 마쳐야 Join이 진행되므로 양쪽 정렬의 속도가 비슷해야 대기시간이 줄어든다. 데이터 양을 고려하는 것도 포인트가 될 수 있다.
- DB 엔진 내부에서는 정렬을 위한 메모리 공간이 필요한데, 이 공간이 적절하게 확보되는 것도 필요하다.

## 시간복잡도 계산방법 예시

<img src="https://github.com/user-attachments/assets/21d02533-ed28-4885-883c-dc7735bd0396" alt="adder" width="70%" />

---

### 참고자료

[(4부) SQL로 데이터 조회하기! join의 의미와 여러 종류의 join들을 쉽게 정리해서 설명합니다! join을 사용한 예제도 있으니까요, 쉬운 설명 들어보세용~ :)](https://youtu.be/E-khvKjjVv4?si=W7w3X4bm7QmFh6ZN)

[How do nested loop, hash, and merge joins work? Databases for Developers Performance #7](https://youtu.be/pJWCwfv983Q?si=4HvQV-H4CxvKLuiT)