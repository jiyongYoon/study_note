# Lateral Join

해당 글에서는 [인프런 JPA 개발자를 위한 고성능 SQL 강의](https://www.inflearn.com/course/vlad-high-performance-sql/dashboard) 일부를 보고 학습한 내용을 정리합니다.

---

# 1. Lateral Join

- FROM 절의 서브쿼리(인라인 뷰)가 외부 쿼리(선행 테이블)의 각 행을 참조하여 `동적으로 결과를 생성`하는 SQL 기능
  - 때문에 선행 테이블의 행이 많으면 성능이 느릴 수 있음
- `상관 서브쿼리`처럼 행 단위로 데이터를 처리할 때 사용

# 2. 실제 동작 방식

```sql
select *
from customer c
join lateral (
    select count(*) as rental_count
    from rental r
    where r.customer_id = c.customer_id
) r on true;
```

customer 테이블의 모든 행마다 각 행의 customer_id와 같은 rental 테이블의 행의 개수를 조합하여 보여준다.

같은 결과를 내는 상관 서브쿼리는 아래와 같다.
```sql
select c.customer_id,
       (select count(*)
        from rental r
        where r.customer_id = c.customer_id) as rental_count
from customer c;
```

# 3. Lateral Join의 유용한 사용처

## 1) 각 행의 TOP-N 구하기

- 각 고객의 가장 최근 rental 1건
```sql
select c.customer_id, r.rental_date
from customer c
left join lateral (
    select rental_date
    from rental r
    where r.customer_id = c.customer_id
    order by rental_date desc
    limit 1
) r on true;
```

> ### 집계 함수와의 차이
> 최근 rental 날짜만 필요하면 아래 sql도 가능하다.
> ```sql
> SELECT c.customer_id, MAX(r.rental_date)
> FROM customer c
> LEFT JOIN rental r ON c.customer_id = r.customer_id
> GROUP BY c.customer_id;
> ```
> 
> 그러나 핵심적인 차이는 `Lateral Join`은 `행 전체를 가지고 올 수 있다`는 점이다.
> 
> ```sql
> SELECT c.customer_id, r.rental_id, r.inventory_id, r.staff_id, r.rental_date
> FROM customer c
> LEFT JOIN lateral (
>   SELECT *
>   FROM rental r
>   WHERE r.customer_id = c.customer_id
>   ORDER BY rental_date DESC
>   LIMIT 1
> ) r ON true;
> ```
> ### 성능 관점
> `MAX() + GROUP BY`는 rental 테이블 전체를 스캔한다. 따라서 대량 데이터에 유리하며, 통계 및 집계용으로 사용된다.
> 반면 `LATERAL + ORDER BY + LIMIT` 조합은 각 customer row 마다 탐색을 하게 되는데, `(customer_id, rental_date DESC)`로 인덱스가 있으면 
> 인덱스 스캔을 하게 되고, `LIMIT 1`이기 때문에 매우 빠르게 데이터 한 건에만 접근할 수 있다.

## 2) 행마다 다른 LIMIT / ORDER BY

```sql
select p.product_id, d.*
from product p
join lateral (
    select *
    from discount d
    where d.category = p.category
    order by d.rate desc
    limit p.max_discount
) d on true;
```

## 3) JSON / 배열 / 함수 결과 펼치기

### 테이블 및 데이터
```sql
create table orders (
    order_id bigint,
    items jsonb
);
```
```json
[
  { "product_id": 101, "qty": 2, "price": 5000 },
  { "product_id": 202, "qty": 1, "price": 12000 }
]
```

### lateral jsonb_array_elements
```sql
select o.order_id, x.item
from orders o
cross join lateral jsonb_array_elements(o.items) as x(item);
```
```text
order_id | item
---------+-----------------------------------
1        | {"product_id":101,"qty":2,"price":5000}
1        | {"product_id":202,"qty":1,"price":12000}
```
- json 배열이 한 행씩 풀림 

### json
```sql
select o.order_id,
       i.product_id,
       i.qty,
       i.price
from orders o
cross join lateral jsonb_to_recordset(o.items) as i(
    product_id int,
    qty int,
    price int
);
```
```text
order_id | product_id | qty | price
---------+------------+-----+-------
1        | 101        | 2   | 5000
1        | 202        | 1   | 12000
```
- 아예 json 필드까지 각 컬럼으로 테이블처럼 펼쳐짐