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

### 캡슐화

- 데이터 + 관련기능 묶기
  - 객체가 기능을 어떻게 구현했는지는 외부에 감추는 것
  - 정보 은닉 포함

- **절차 지향적 코드**는
```java
if (acc.getMembership() == REGULAR && acc.getExpDate().isAfter(now)) {
    ...정회원 기능
}
```
해당 기능이 `이벤트로 인해 5년 이상 사용자들은 일부 기능을 정회원 혜택으로 1개월 무상 제공한다`고 수정된다면
```java
if (acc.getMembership() == REGULAR &&
(
  (acc.getServiceDate().isAfter(fiveYearAgo) && acc.getExpDate().isAfter(now())) ||
  (acc.getServiceDate().isBefore(fiveYearAgo) && addMonth(acc.getExpDate()).isAfter(now()))
  )
)
{
    ...정회원 기능
}
```
또한, 이 데이터들을 직접 사용하는 곳에는 동일한 수정이 또 필요해진다.

- **객체 지향적 코드**는 해당 데이터를 `객체`에 감싸고, 프로시저가 직접 객체의 데이터에 접근하지 않고, 데이터에 접근하는 기능만 열어두어 활용하게 한다.
```java
public class Account {
    private Membership membership;
    private Date expDate;
    
    public boolean hasRegularPermission() {
        return membership == REGULAR && expDate.isAfter(now());
    }
}

// 사용처
if (acc.hasRegularPermission()) {
    ...정회원 기능
}
```
해당 기능 수정시 `hasRegularPermission()` 메서드 내부만 변경해주면 된다.
> 캡슐화를 하면
> 1. 요구사항의 변화가 발생한 `객체`에만 변경이 발생하며, 해당 프로시저 및 데이터를 사용하는 코드에는 영향이 최소화 된다!!
> 2. 캡슐화를 하면 해당 `기능에 대한 의도`를 분명하게 할 수 있다.

### 캡슐화를 위한 규칙

1. 데이터를 달라고 해서 내가 판단하지 말고, 해당 데이터를 가진 객체에게 처리 해달라고 하기

    `if (acc.getMembership() == REGULAR) {}` -> `if (acc.hasRegularPermission()) {}`

2. Demeter's Law - 인스턴스 객체, 파라미터로 받은 객체, 필드로 참조하는 객체의 메서드만 호출해라

    `acc.getExpDate().isAfter(now)` -> `acc.isExpired()`

### 캡슐화 연습 1

```java
public class Movie {
    public static int REGULAR = 0;
    public static int NEW_RELEASE = 1;
    private int priceCode;
    
    public int getPriceCode() {
        return priceCode;
    }
}

public class Rental {
    private Movie movie;
    private int daysRented;
    
    public int getFrequentRenterPoints() {
        if (movie.getPriceCode() == Movie.NEW_RELEASE &&
        daysRented > 1) {
            return 2;
        } else {
            return 1;
        }
    }
}
```
- 캡슐화를 적용하면
```java
public class Movie {
    public static int REGULAR = 0;
    public static int NEW_RELEASE = 1;
    private int priceCode;
    
    public int getPriceCode() {
        return priceCode;
    }
    
    // Movie 객체에 데이터를 가지고 일을 시키는 부분.
    // 일에 필요한 데이터는 파라미터로 전달해줌!
    public int getFrequentRenterPoints(int daysRented) {
        if (priceCode == NEW_RELEASE &&
        daysRented > 1) {
            return 2;
        } else {
            return 1;
        }
    }
}

public class Rental {
    private Movie movie;
    private int daysRented;
    
    public int getFrequentRenterPoints() {
        return movie.getFrequentRenterPoints(daysRented);
    }
}
```

### 캡슐화 연습 2

```java
public class Timer {
    public long startTime;
    public long stopTime;
}

public class Main {
    Timer t = new Timer();
    t.startTime = System.currentTimeMillis();
    ...
    t.stopTime = System.currentTimeMillis();
    
    log elaspedTime = t.stopTime - t.startTime;
}
```
- 캡슐화를 적용하면
```java
public class Timer {
    public long startTime;
    public long stopTime;
    
    public void start() {
        this.startTime = System.currentTimeMillis();
    }
    
    public void stop() {
        this.stopTime = System.currentTimeMillis();
    }
    
    public long elaspedTime(TimeUnit timeUnit) {
        switch (timeUnit) {
            case MILLISECOND:
                return (this.stopTime - this.startTime) * 10;
            ...
        }
    }
}

public class Main {
    Timer t = new Timer();
    t.start();
    ...
    t.stop();
    
    log elaspedTime = t.elaspedTime(TimeUnit.MILLISECOND);
}
```

### 캡슐화 연습 3

- 어떤 데이터를 가지고 판단한 후, 조건에 만족하면 데이터를 변경하는 코드 동작 방식
```java
public class Main {
    public void verifyEmail(String token) {
        Member member = findByToken(token);
        if (member == null) throw new BadTokenException();
        
        if (member.getVerificationEmailStatus() == 2) {
            throw new AlreadyVerifiedException();
        } else {
            member.setVerificationEmailStatus(2);
        }
    }
}
```
- 캡슐화를 적용하면
```java
public class Member {
    private int verificationEmailStatus;
    
    public void verifyEmail() {
        if (isEmailVerified()) {
            throw new AlreadyVerifiedException();
        } else {
            member.setVerificationEmailStatus(2);
        }
    }
    
    public boolean isEmailVerified() {
        return verificationEmailStatus == 2;
    }
}
```