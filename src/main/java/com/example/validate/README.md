# @Valid

- 자바 진영 표준 스펙
- 빈 검증기를 이용해 객체의 제약 조건을 검증
- 어노테이션으로 편리하게 검증하는 것이 특징

Springboot 어플리케이션에서는 모든 요청은 프론트 컨트롤러인 `디스패처 서블릿`을 통해서 컨트롤러에 전달된다.
전달되는 과정에서 컨트롤러 메서드의 객체를 만들어주는 `ArgumentResolver`가 동작하는데, `@Valid` 역시 `ArgumentResolver`에 의해 처리된다.
검증 오류가 있다면 `MethodArgumentNotValidExcpetion` 예외가 발생한다.

때문에, `@Valid`는 기본적으로 컨트롤러에서만 동작한다.

> ArgumentResolver?
> 
> 대표적으로 `@RequestBody`의 Json 메시지를 객체로 변환하는 역할을 한다.
> 변환 시 이 내부에서 `@Valid` 어노테이션이 있으면 유효성 검사를 진행한다.

### 예시

- Dto
    ```java
    @Getter
    public class CreateDto {
    
        @Null
        private Long id;
    
        @Email
        private String email;
    
        @NotBlank
        private String name;
    
        private String description;
    
    }
    ```
- Controller
    ```java
    @RestController
    @RequestMapping
    public class ValidateController {
        @PostMapping("/valid")
        public CreateDto validate(@Valid @RequestBody CreateDto createDto) {
        }
    }
    ```
- Request
    ```json
    POST http://localhost:8080/valid
    Content-Type: application/json

    {
      "id": 1,
      "email": "email",
      "name": "",
      "description": ""
    }
    ```

- Response
    ```json
    http://localhost:8080/valid

    HTTP/1.1 400
    Content-Type: application/json
    Transfer-Encoding: chunked
    Date: Wed, 27 Mar 2024 00:19:29 GMT
    Connection: close

    {
      "timestamp": "2024-03-27T00:19:29.310+00:00",
      "status": 400,
      "error": "Bad Request",
      "path": "/valid"
    }
    ```

- Log
    ```text
    WARN 39896 --- [nio-8080-exec-5] .w.s.m.s.DefaultHandlerExceptionResolver : 
    Resolved [org.springframework.web.bind.MethodArgumentNotValidException: 
    Validation failed for argument [0] in public com.example.validate.CreateDto com.example.validate.ValidateController.validate(com.example.validate.CreateDto) with 3 errors: 
    [Field error in object 'createDto' on field 'id': rejected value [1]; codes [Null.createDto.id,Null.id,Null.java.lang.Long,Null]; 
    arguments [org.springframework.context.support.DefaultMessageSourceResolvable: codes [createDto.id,id]; arguments []; default message [id]]; default message [널이어야 합니다]] 
    [Field error in object 'createDto' on field 'name': rejected value []; codes [NotBlank.createDto.name,NotBlank.name,NotBlank.java.lang.String,NotBlank]; 
    arguments [org.springframework.context.support.DefaultMessageSourceResolvable: codes [createDto.name,name]; arguments []; default message [name]]; default message [공백일 수 없습니다]] 
    [Field error in object 'createDto' on field 'email': rejected value [email]; codes [Email.createDto.email,Email.email,Email.java.lang.String,Email]; 
    arguments [org.springframework.context.support.DefaultMessageSourceResolvable: codes [createDto.email,email]; arguments []; default message [email],[Ljavax.validation.constraints.Pattern$Flag;@545448,.*]; default message [올바른 형식의 이메일 주소여야 합니다]] ]
    ```
  
# @Validated

- Spring에서 AOP 기반으로 유효성 검증을 진행하는 어노테이션 기능 (Spring 프레임워크에서 제공하는 기능임)
- 어플리케이션에 들어오는 입력 파라미터의 검증이 아닌, 개발 과정에서 메서드 파라미터를 검증하고 싶은 경우 사용 가능
- springboot 내부에서 Exception이 발생한다.

### 예시

- Dto (동일)
  ```java
  @Getter
  public class CreateDto {
  
      @Null
      private Long id;
  
      @Email
      private String email;
  
      @NotBlank
      private String name;
  
      private String description;
  
  }
  ```

- Controller
  ```java
  @RestController
  @RequestMapping
  @RequiredArgsConstructor
  public class ValidateController {
  
      private final ValidateService validateService;
  
      @PostMapping("/validated")
      public CreateDto validate(@RequestBody CreateDto createDto) {
          return validateService.validate(createDto);
      }
  
  }
  ```

- Service
  ```java
  @Service
  @Validated
  public class ValidateService {
  
      public CreateDto validate(@Valid CreateDto createDto) {
          System.out.println("service = " + createDto);
          return createDto;
      }
  }
  ```
  
- Request
  ```json
  POST http://localhost:8080/validated
  Content-Type: application/json
  
  {
    "id": 1,
    "email": "email",
    "name": "",
    "description": ""
  }
  ```
  
- Response
  ```json
  HTTP/1.1 500 
  Content-Type: application/json
  Transfer-Encoding: chunked
  Date: Wed, 27 Mar 2024 00:27:08 GMT
  Connection: close
  
  {
    "timestamp": "2024-03-27T00:27:08.894+00:00",
    "status": 500,
    "error": "Internal Server Error",
    "path": "/validated"
  }
  ```
  
- Log
  ```text
  2024-03-27 09:27:08.893 ERROR 28852 --- [nio-8080-exec-2] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed; nested exception is javax.validation.ConstraintViolationException: validate.createDto.email: 올바른 형식의 이메일 주소여야 합니다, validate.createDto.name: 공백일 수 없습니다, validate.createDto.id: 널이어야 합니다] with root cause

  javax.validation.ConstraintViolationException: validate.createDto.email: 올바른 형식의 이메일 주소여야 합니다, validate.createDto.name: 공백일 수 없습니다, validate.createDto.id: 널이어야 합니다
      at org.springframework.validation.beanvalidation.MethodValidationInterceptor.invoke(MethodValidationInterceptor.java:120) ~[spring-context-5.3.29.jar:5.3.29]
      at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186) ~[spring-aop-5.3.29.jar:5.3.29]
      at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:763) ~[spring-aop-5.3.29.jar:5.3.29]
      at org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:708) ~[spring-aop-5.3.29.jar:5.3.29]
      at com.example.validate.ValidateService$$EnhancerBySpringCGLIB$$f883b6fe.validate(<generated>) ~[main/:na]
      at com.example.validate.ValidateController.validate(ValidateController.java:27) ~[main/:na]
      at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:na]
      at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62) ~[na:na]
  ...
  ...
  ```
  
# 커스텀 어노테이션으로 직접 유효성 검사하기

- 기본 커스텀이 아닌 비즈니스로직에 관련되거나 커스텀한 파라미터 검증을 하고 싶을때 사용
- Validator 클래스(실제 검증로직이 들어갈)는 `JSR`에서 제공하는 `javax.validation`의 `ConstraintValidator` 인터페이스를 구현해야함
- Controller 클래스에서 `@Valid`, `@Validated` 모두 적용되지만, 그룹 설정을 하려면 `@Validated` 어노테이션을 사용해야함.

### 예시

- Validate 어노테이션
  ```java
  @Target({ElementType.FIELD})
  @Retention(RetentionPolicy.RUNTIME)
  @Constraint(validatedBy = PasswordValidator.class) // 어떤 클래스를 사용해서 검증 할지
  @Documented
  public @interface Password {
  
      String message() default "패스워드 형태가 올바르지 않습니다."; // 검증 실패 시 반환할 메시지
  
      Class<?>[] groups() default {}; // 유효성 검증이 진행될 그룹
  
      Class<? extends Payload>[] payload() default {}; // 유효성 검증 시 전달할 메타 정보
  }
  ```

- Validator 검증 클래스
  ```java
  @Slf4j
  public class PasswordValidator implements ConstraintValidator<Password, Object> {
  
      /**
       * 초기화하기 위한 메서드. 검증 로직이 동작하면 맨 먼저 해당 메서드를 통해 필요한 정보를 초기화한다.
       * 즉, 검증로직이 동작할때마다 해당 객체는 생성되었다가 사라지게 된다.
       * @param constraintAnnotation annotation instance for a given constraint declaration
       */
      @Override
      public void initialize(Password constraintAnnotation) {
          ConstraintValidator.super.initialize(constraintAnnotation);
      }
  
      /**
       * 실제 유효성을 검증하는 메서드. 클래스의 제네릭에 따라 타입을 지정해서 받을 수 있다.
       * @param value object to validate
       * @param context context in which the constraint is evaluated
       *
       * @return
       */
      @Override
      public boolean isValid(Object value, ConstraintValidatorContext context) {
          String password = String.valueOf(value);
  
          boolean hasLetter = false;
          boolean hasDigit = false;
          boolean hasSpecialChar = false;
  
          for (char c : password.toCharArray()) {
              if (Character.isLetter(c)) {
                  hasLetter = true;
              } else if (Character.isDigit(c)) {
                  hasDigit = true;
              } else if (!Character.isWhitespace(c)) { // 공백 제외한 다른 특수문자
                  hasSpecialChar = true;
              }
          }
  
          int includeCount = 0;
          if (hasLetter) includeCount++;
          if (hasDigit) includeCount++;
          if (hasSpecialChar) includeCount++;
  
          log.info(
                  "password valid, hasLatter={}, hasDigit={}, hasSpecialChar={}, includeCount={}",
                  hasLetter,
                  hasDigit,
                  hasSpecialChar,
                  includeCount);
  
          return includeCount >= 2;
      }
  }
  ```

- group 지정을 할 인터페이스 (선택사항)
  ```java
  public interface Create extends Default {}
  ```
  - 가독성이 떨어진다는 단점이 있어 생각해보고 적용할 것.
  - 동일한 DTO를 사용하려면 사용하고, 그렇지 않다면 DTO를 분리하여 검증하는 것이 방법이 될 것.

- Dto
  ```java
  @Getter
  @ToString
  public class CreateDto {
  
      @Null
      private Long id;
  
      @Email
      private String email;
  
      @Size(min = 5)
      @Password(groups = Create.class)
      private String password;
  
      @NotBlank
      private String name;
  
      private String description;
  
  }
  ```

- Controller
  ```java
  @RestController
  @RequestMapping
  @RequiredArgsConstructor
  public class ValidateController {
  
      private final ValidateService validateService;
  
      @PostMapping("/valid")
      public CreateDto valid(@Valid @RequestBody CreateDto createDto) {
          System.out.println("controller = " + createDto);
          return createDto;
      }
  
      @PostMapping("/validated")
      public CreateDto validate(@RequestBody CreateDto createDto) {
          System.out.println("controller = " + createDto);
          return validateService.validate(createDto);
      }
  
      @PostMapping("/validated/group")
      public CreateDto validGroup(@Validated(Create.class) @RequestBody CreateDto createDto) {
          System.out.println("controller = " + createDto);
          return createDto;
      }
  }
  ```
  
- Request (그룹 적용 예시1)
  ```java
  ### 커스텀 validation group 적용
  POST http://localhost:8080/validated/group
  Content-Type: application/json
  
  {
    "email": "email@email",
    "name": "hello",
    "password": "123",
    "description": ""
  }
  ```
  - `@Size` 와 `@Password` 두 어노테이션 모두 동작
  - Log
    ```text
    Resolved [org.springframework.web.bind.MethodArgumentNotValidException: Validation failed for argument [0] in public com.example.validate.CreateDto com.example.validate.ValidateController.validGroup(com.example.validate.CreateDto) with 2 errors: 
    [Field error in object 'createDto' on field 'password': rejected value [123]; codes [Size.createDto.password,Size.password,Size.java.lang.String,Size]; arguments [org.springframework.context.support.DefaultMessageSourceResolvable: codes [createDto.password,password]; arguments []; default message [password],2147483647,5]; default message [크기가 5에서 2147483647 사이여야 합니다]] 
    [Field error in object 'createDto' on field 'password': rejected value [123]; codes [Password.createDto.password,Password.password,Password.java.lang.String,Password]; arguments [org.springframework.context.support.DefaultMessageSourceResolvable: codes [createDto.password,password]; arguments []; default message [password]]; default message [패스워드 형태가 올바르지 않습니다.]] ]
    ```
  
- Request (그룹 적용 예시2)
  ```java
  ### 커스텀 validation group 적용
  POST http://localhost:8080/validated/group
  Content-Type: application/json
  
  {
    "email": "email@email",
    "name": "hello",
    "password": "123!",
    "description": ""
  }
  ```
  - `@Size` 와 `@Password` 두 어노테이션 모두 동작하지만, `@Size`만 걸림 
  - Log
    ```text
    Resolved [org.springframework.web.bind.MethodArgumentNotValidException: Validation failed for argument [0] in public com.example.validate.CreateDto com.example.validate.ValidateController.validGroup(com.example.validate.CreateDto): 
    [Field error in object 'createDto' on field 'password': rejected value [123!]; codes [Size.createDto.password,Size.password,Size.java.lang.String,Size]; arguments [org.springframework.context.support.DefaultMessageSourceResolvable: codes [createDto.password,password]; arguments []; default message [password],2147483647,5]; default message [크기가 5에서 2147483647 사이여야 합니다]] ]
    ```

- Request (그룹 적용 안한 예시)
  ```text
  ### 커스텀 validation
  POST http://localhost:8080/valid
  Content-Type: application/json
  
  {
  "email": "email@email",
  "name": "hello",
  "description": ""
  }
  ```
  - 통과함.
  - Controller의 `@Validate(Create.class)` 가 없기 때문에, `@Password` 검증이 적용되지 않음

### 참고자료

[망나니개발자 블로그](https://mangkyu.tistory.com/174)