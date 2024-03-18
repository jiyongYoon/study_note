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

---

## 다형성과 추상화

### 다형성 (Polymorphism)

- 여러 모습을 갖는 것 -> 한 객체가 여러 타입을 갖는 것.
  - 객체지향에서는 상속을 활용해 다형성을 구현할 수 있다.

### 추상화 (Abstraction)

- 데이터나 프로세스 등을 의미가 비슷한 개념이나 의미있는 표현으로 정의하는 과정
- 방법1: 특정한 성질을 이용하여 추출
- 방법2: 공통 성질을 이용하여 추출 -> 다형성과 연관

- 예시
  - 실물세계의 단어 -> 지포스, 라데온 등등이 GPU라는 단어로 추상화됨 (방법2)
  - DB의 테이블과 그 속성들 -> 속성들이 DB의 테이블로 추상화됨 (방법1)
  - Java의 클래스 -> 내부 필드(email, name 등)들이 클래스(Member)로 추상화됨 (방법1)

### 타입 추상화

- 여러 구현 클래스를 대표하는 상위 타입을 도출하며, 흔히 `인터페이스`로 추상화 한다.
- 추상화 클래스는 기능에 대한 의미만을 제공하며, 추상화 클래스를 상속하여 기능을 구현한다.

<img src="https://github.com/jiyongYoon/study_note/assets/98104603/fcd85d40-b746-4358-b6f5-e82f070f19bc" alt="adder" width="70%" />

- 추상화를 사용하는 가장 큰 이유는 `유연함`이다.
- 아래 예시에서는 구현체를 직접 사용했을 때, 요구사항이 추가되면 나타나는 상황이다.
```java
// sms 발송만
public class Main {
    private SmsSender smsSender;
    
    public void cancel(String ono) {
        // 주문 취소 처리
        
        sms.Sender.sendSms();
    }
}
```

```java
// 카카오 푸시 발송이랑, 메일 전송도!
public class Main {
    private SmsSender smsSender;
    private KakaoPush kakaoPush;
    private MailService mailService;
    
    public void cancel(String ono) {
        // 주문 취소 처리
        
        if (pushEnable) {
            kakaoPush.push();
        } else {
            sms.Sender.sendSms();
        }
        mailService.sendMail();
    }
}
```
- 요구 사항 변경에 따라 주문 취소 코드가 함께 변경됨.

--> 추상화를 적용한다면?

- SMS전송, 카카오톡 푸시, 이메일 세 가지 기능을 `Notifier`로 추상화 적용!

```java
public class Main {
    public void cancel(String ono) {
        // 주문 취소 처리
        
        Notifier notifier = getNotifier();
        notifier.notify();
    }
    
    private Notifier getNotifier() {
        if (pushEnable) {
            return new KakaoNotifier();
        } else {
            return new SmsNotifier();
        }
    }
}
```

--> 객체를 생성하는 `getNotifier()` 메서드 부분을 추상화를 한번 더 한다면 

```java
public interface NotifierFactory {
    Notifier getNotifier();
    
    static NotifierFactory instance() {
        return new DefaultNotifierFactory();
    }
}

public class DefaultNotifierFactory implements NotifierFactory {
    public Notifier getNotifier() {
        if (pushEnable) {
            return new KakaoNotifier();
        } else {
            return new SmsNotifier();
        }
    }
}
```

- 통지 방식을 변경하게 된다면, `DefaultNotifierFactory` 부분 코드만 수정하면 된다.
- `주문 취소 처리 로직` 자체는 변하지 않는다는 것이 핵심!

> **그럼 이 좋은걸 언제 하나?**
> 
> 추상화는 유연함을 얻고 프로그램의 단순함을 잃는다. <br> 
> trade-off가 존재하기 때문에, 실제 변경 및 확장이 발생할 때 추상화를 시도하는 것이 잘못된 방법으로 추상화를 하지 않으면서 <br>
> 효율적으로 할 수 있는 시기가 되겠다!

---

## 상속보단 조립

### 상속

- 가장 큰 특징: 상위 클래스의 기능에 의존적
- 이로 인해 발생하는 단점
  - 상위 클래스 변경이 어렵게 될 수 있다. 모든 하위 클래스에 영향을 줄 수 있기 때문이다.
  - 클래스 갯수가 많아진다. 새로운 조합이 필요한 경우 기존 상위 클래스의 다른 분기에서 구현한 클래스를 상속해야하며, 어떤 클래스를 상속해야 하는지도 애매해질 수 있음.
  - 오용이 쉽다. 상위 클래스들의 구현과 메서드를 다 알아야 정확한 기능을 구현할 수 있는데, 쉽지 않다.

### 조립

- 여러 객체를 묶어서 더 복잡한 기능을 제공
- 이로 인해 발생하는 장점
  - 필요한 기능만을 뽑아서 사용하는 것이 수월해짐
  - 상속으로 인한 단점을 보완

> 진짜 하위 타입인 경우에만 상속을 사용! (is a) <br>
> 조립이 가능한지를 먼저 검토할 것!

---

## 기능과 책임 분리

- 기능을 먼저 리스트업 하고나면, 해당 기능을 누가 제공할 것인지를 고민하는 것이 필요함

<img src="https://github.com/jiyongYoon/study_note/assets/98104603/48f1fd6f-7660-49ac-b2fa-59a423827a01" alt="adder" width="70%" />

  - 위 그림은 `암호 변경`이라는 목표기능을 위해 세부적으로 필요한 기능을 리스트업하고, 세부 기능들의 책임을 나눈 모습이다.
  - 이 분배로 코드를 구현하면 아래와 같이 구현할 수 있다.
    
    ```java
    public class ChangePasswordService {
        public Result changePassword(String id, String oldPw, String newPw) {
            Member member = memberRepository.findOne(id);
            if (member == null) {
                return Result.NO_MEMBER;
            }
            try {
                member.changePassword(oldPw, newPw);
                return Result.SUCCESS;
            } catch (BadPasswordException ex) {
                return Result.BAD_PASSWORD;
            }
        }
    }
    ```
    - `ChangePasswordService`는 각각의 객체를 활용해서 `암호변경`이라는 기능을 완성하고 있다.
    - 그러나 이런 조립도 클래스와 메서드가 커지게 되면 결국 절차지향적으로 변하게 된다. -> 계속해서 `책임분배/분리가 필요`한 상황인 것이다.
    
> **책임 분배/분리 방법**
> 
> 1) 패턴 적용
> 2) 계산 기능 분리
> 3) 외부 연동 분리
> 4) 조건별 분기는 추상화

1. 패턴 적용
- 전형적인 역할 분리
  - MVC 패턴, DDD, AOP, GoF 디자인 패턴, 클린 아키텍처 등

2. 계산 기능 분리
- 계산하는 작업만 클래스로 따로 분리

3. 외부 연동 분리
- 네트워크, 메시징, 파일 등의 연동 처리 클래스 분리

4. 조건별 분기 추상화
```java
public class Main {
    String fileUrl = "";
    if (fileId.startWith("local:")) {
        fileUrl = "/files/" + fileId.substring(6);
    } else if (fileId.startWith("ss:")) {
        fileUrl = "http://fileserver/files/" + fileId.substring(3);    
    }
    ...
}
```
-> 추상화를 적용하면
```java
public class Main {
    FileInfo fileInfo = FileInfo.getFileInfo(fileUrl);
    String fileUrl = fileInfo.getUrl();
}

public interface FileInfo {
    String getUrl();
    static FileInfo getFile();
}

public class SSFileInfo implements FileInfo {
    private String fileId;
    
    public String getUrl() {
        return "http://fileserver/files/" + fileId.substring(3);
    }
}
```

---

## 의존과 DI

- 기능 구현을 위해 다른 구성요소를 사용하는 것.
- 의존하는 대상이 바뀌면 내가 바뀔 가능성이 높아짐. -> 의존성이 높다.

### 의존 대상이 많아지는 경우

- 한 클래스에서 많은 기능을 제공할수록
  - 기능별로 분리를 고려하면 클래스는 늘어나나 의존성이 줄어들어 유지보수, 개별기능 테스트 등에 유리해짐
- 기능이 제대로 묶이지 않은 경우
  - 몇 가지 의존 대상을 단일 기능으로 묶어서 의존 대상을 줄일 수 있음

### 의존 대상 객체를 직접 생성하면?

- 생성 클래스가 바뀌면 의존하는 코드도 바뀌게 됨.
  - 추상화를 통해 구현체를 직접 의존하지 않도록 하여 결합도를 낮출 수 있음
- `팩토리`, `빌더`, `의존 주입(DI)`, `서비스 로케이터` 등의 방법을 활용하여 의존 대상 객체를 직접 생성하지 않을 수 있다.

### 의존 주입(DI)의 장점

- 상위 타입을 사용하면 의존 대상이 바뀔 때 설정(구현체)만 변경하면 됨.
- 의존하는 객체 없이 대역 객체를 사용하여 기능 테스트가 가능해짐.

---

## DIP(Dependency Inversion Principle)

### 고수준 모듈

- 의미 있는 단일 기능을 제공
- 상위 수준의 정책 구현

### 저수준 모듈

- 고수준 모듈의 기능을 구현하기 위해 필요한 하위 기능의 실제 구현

### 예시

- 수정한 도면 이미지를 NAS에 저장하고, 측정 정보를 DB 테이블에 저장하고 수정 의뢰 정보를 DB에 저장하는 기능이 있다면,
- 고수준
  - 도면 이미지 저장
  - 측정 정보 저장
  - 도면 수정 의뢰
- 저수준
  - NAS에 이미지 저장
  - MEAS_INFO 테이블에 저장
  - BP_MOD_REQ 테이블에 저장

### 고수준이 저수준에 직접 의존하면?

- 저수준 모듈 변경이 고수준 모듈에 영향을 준다.

```java
public class MeasureService {
    public void measure(MeasureReq req) {
        File file = req.getFile();
        nasStorage.save(file);
        ...
    }
}
-> 변경 시
public class MeasureService {
    public void measure(MeasureReq req) {
        File file = req.getFile();
        s3Storage.upload(file);
        ...
    }
}
```
- 고수준 정책이 바뀌지 않았으나 저수준 구현 변경으로 코드 변경이 발생했다.

> `의존 역전 원칙(Dependency Inversion Principle)`을 적용하면,
> 
> 고수준 모듈은 저수준 모듈의 구현에 의존하면 안되고, 저수준 모듈이 고수준 모듈에서 정의한 추상타입에 의존해야 한다는 원칙!

<img src="https://github.com/jiyongYoon/study_note/assets/98104603/150b7ef4-e031-4872-be6e-47ce0e7b5f2b" alt="adder" width="70%" />

### 추상화는 고수준 관점에서 할 것!

<img src="https://github.com/jiyongYoon/study_note/assets/98104603/ce7aaedb-c214-4c89-9396-54592af60404" alt="adder" width="70%" />

### 왜?
  - 우리는 고수준을 변경할게 아니라, 저수준을 변경할 것이기 때문이다.
  - 왼쪽처럼 저수준 기능을 추상화한다면, `ExceptionAdvice` 입장에서는 다른 저수준 모듈을 사용하는 것이 부자연스러워진다.
  - **반면, 오른쪽처럼 고수준이 사용할 기능을 추상화한다면, `ExceptionCollector`의 실제 구현체인 저수준 모듈을 갈아끼워도, 고수준 모듈에 영향이 없다!**