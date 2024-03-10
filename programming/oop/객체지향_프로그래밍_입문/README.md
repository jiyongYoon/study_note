# 객체 지향 프로그래밍 입문 - 최범균

---

## 왜 객체 지향을 공부하나?

- 비용 측면
  - 코드 분석시간 증가
  - 코드 변경시간 증가
- 변화 측면
  - 계속 변화하는 요구에 발빠르게 대응되어야 함

> 이런 이유로 인해 패러다임, 설계 및 아키텍처, 업무 프로세스 및 문화 등으로 이런 대응을 하게 된다.
> 그 중, 객체 지향은 개발의 `패러다임` 중 일부이며, `캡슐화` 및 `다형성(추상화)`라는 방법으로 비용을 줄이고 빠르게 변화할 수 있게 한다.

---

## 객체?

### 절차 지향?

<img src="https://github.com/jiyongYoon/study_note/assets/98104603/2843ab14-0226-4a9b-af40-8cd4d2b52fd7" alt="adder" width="70%" />

- 프로시저는 java의 메서드라고 이해하면 될 것이다.
- 즉, 절차 지향은 `메서드가 직접 어떤 데이터에 접근하는 것`이다.

```java
// 인증 API
Account account = findOne(id);
if (account.getState() == DELETED) {
    // 로직
}

// 암호변경 API
Account account = findOne(id);
if (account.getState() == DELETED) {
    // 로직
}
```
- 요구 사항이 변경되면
```java
// 인증 API
Account account = findOne(id);
if (account.getState() == DELETED ||
    account.getBlockCount() > 0) { <- 추가
    // 로직
}

// 암호변경 API
Account account = findOne(id);
if (account.getState() == DELETED ||
    account.getBlockCount() > 0) { <- 추가
    // 로직
}
```
- 이런 식으로 요구사항이 변하면 계속 변경되어야 하며, 해당 변경점이 여러 곳에 매번 반영되어야 한다.

### 객체 지향?

<img src="https://github.com/jiyongYoon/study_note/assets/98104603/1f517d44-1975-4957-ba1c-c855c40722cb" alt="adder" width="70%" />

- 데이터와 프로시저를 `객체`라는 단위로 묶고, 데이터는 `객체`를 통해서만 접근을 하도록 한다.
- 즉, 객체는 `기능` 단위로 묶이게 되며, 외부에 해당 `기능`을 `기능 명세(메서드)`의 형태로 제공하게 된다.
- 객체와 객체는 `메시지`를 주고 받는다고 표현하며, 메시지는 `기능을 호출`하거나, `결과를 전달`하거나, `오류를 뱉거나` 등이 메시지라고 불리는 것이다.
  - 객체끼리의 상호작용!

- ex: 회원 객체
  - 암호 변경하기 기능
  - 차단 여부 확인하기 기능