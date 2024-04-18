# lobmok의 Builder, 어떻게 컴파일 될까?

> 정리
>
> `클래스 레벨`에 어노테이션을 사용하는 경우, <br>
> 모든 변수를 담은 생성자가 필요하며, final과 같이 값을 할당할 필요가 없는 경우는 빌더 패턴에서 제외한다.
>
> `생성자 레벨`에 어노테이션을 사용하는 경우, <br>
> 해당 생성자를 사용할 수 있는 파라미터만을 담은 빌더 패턴을 생성한다. final 및 다른 생성자와 관계 없다.

## 클래스 레벨의 @Builder

### 예시 1

- 기본적인 Dto 형태의 클래스
- 필드를 모두 포함하는 생성자가 생기며, 마지막 `build()` 호출에서 생성자를 호출함

**코드**
```java
@Builder
public class ClassLevelDto {
    private String name;
    private int age;
}
```
**컴파일**
```java
public class ClassLevelDto {
    private String name;
    private int age;

    ClassLevelDto(final String name, final int age) {
        this.name = name;
        this.age = age;
    }

    public static ClassLevelDtoBuilder builder() {
        return new ClassLevelDtoBuilder();
    }

    public static class ClassLevelDtoBuilder {
        private String name;
        private int age;

        ClassLevelDtoBuilder() {
        }

        public ClassLevelDtoBuilder name(final String name) {
            this.name = name;
            return this;
        }

        public ClassLevelDtoBuilder age(final int age) {
            this.age = age;
            return this;
        }

        public ClassLevelDto build() {
            return new ClassLevelDto(this.name, this.age);
        }

        public String toString() {
            return "ClassLevelDto.ClassLevelDtoBuilder(name=" + this.name + ", age=" + this.age + ")";
        }
    }
}
```

### 예시 2

- 필드를 모두 포함하는 생성자 없이 일부만 포함하는 생성자가 있는 경우
- `build()` 마지막에 호출할 생성자가 없어 컴파일 에러.
- 이를 통해 알수 있는 것은, 클래스 레벨에 `@Build` 를 붙이면, 모든 필드를 가진 생성자가 필요하다.

**코드**
```java
@Builder
public class ClassLevelDto2 {
    private String name;
    private int age;

    public ClassLevelDto2(String name) {
        this.name = name;
        this.age = 1;
    }
}
```

**컴파일** -> 에러
```text
error: constructor ClassLevelDto2 in class ClassLevelDto2 cannot be applied to given types;
@Builder
^
  required: String
  found: String,int
  reason: actual and formal argument lists differ in length
```

### 예시 3

- final 필드를 가진 클래스
- `build()`에서 마지막에 생성자를 호출하면 되기 때문에 영향없음.
- 그러나, final의 의미를 생각해보면 빌더패턴에서 계속 값이 변할 수 있기 때문에 명시적으로 좋은 형태는 아니라고 생각됨.

**코드**
```java
@Builder
public class ClassLevelDto3 {
    private final String name;
    private int age;
}
```

**컴파일**
```java
public class ClassLevelDto3 {
    private final String name;
    private int age;

    ClassLevelDto3(final String name, final int age) {
        this.name = name;
        this.age = age;
    }

    public static ClassLevelDto3Builder builder() {
        return new ClassLevelDto3Builder();
    }

    public static class ClassLevelDto3Builder {
        private String name;
        private int age;

        ClassLevelDto3Builder() {
        }

        public ClassLevelDto3Builder name(final String name) {
            this.name = name;
            return this;
        }

        public ClassLevelDto3Builder age(final int age) {
            this.age = age;
            return this;
        }

        public ClassLevelDto3 build() {
            return new ClassLevelDto3(this.name, this.age);
        }

        public String toString() {
            return "ClassLevelDto3.ClassLevelDto3Builder(name=" + this.name + ", age=" + this.age + ")";
        }
    }
}
```

### 예시 4

- final 맴버가 초기화되어있는 클래스
- 해당 맴버는 어차피 값의 할당이 불가능하여 생성자 및 빌더에서 제외함

**코드**
```java
@Builder
public class ClassLevelDto4 {
    private final String name = "홍길동";
    private int age;
}
```

**컴파일**
```java
public class ClassLevelDto4 {
    private final String name = "홍길동";
    private int age;

    ClassLevelDto4(final int age) {
        this.age = age;
    }

    public static ClassLevelDto4Builder builder() {
        return new ClassLevelDto4Builder();
    }

    public static class ClassLevelDto4Builder {
        private int age;

        ClassLevelDto4Builder() {
        }

        public ClassLevelDto4Builder age(final int age) {
            this.age = age;
            return this;
        }

        public ClassLevelDto4 build() {
            return new ClassLevelDto4(this.age);
        }

        public String toString() {
            return "ClassLevelDto4.ClassLevelDto4Builder(age=" + this.age + ")";
        }
    }
}
```

---

## 생성자 레벨의 @Builder

### 예시 1

- 기본적인 Dto 형태의 클래스
- 필드를 모두 포함하는 생성자가 생기며, 마지막 `build()` 호출에서 생성자를 호출함.
- 클래스 레벨의 컴파일과 다른바 없음.

**코드**
```java
public class ConstructLevelDto {
    private String name;
    private int age;

    @Builder
    public ConstructLevelDto(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
```

**컴파일**
```java
public class ConstructLevelDto {
    private String name;
    private int age;

    public ConstructLevelDto(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public static ConstructLevelDtoBuilder builder() {
        return new ConstructLevelDtoBuilder();
    }

    public static class ConstructLevelDtoBuilder {
        private String name;
        private int age;

        ConstructLevelDtoBuilder() {
        }

        public ConstructLevelDtoBuilder name(final String name) {
            this.name = name;
            return this;
        }

        public ConstructLevelDtoBuilder age(final int age) {
            this.age = age;
            return this;
        }

        public ConstructLevelDto build() {
            return new ConstructLevelDto(this.name, this.age);
        }

        public String toString() {
            return "ConstructLevelDto.ConstructLevelDtoBuilder(name=" + this.name + ", age=" + this.age + ")";
        }
    }
}
```

### 예시 2

- 생성자 내부에서 특정 필드는 파라미터를 받지 않고 초기화 하는 클래스
- 해당 필드를 제외한 생성자 및 빌더패턴 메서드 생성
- 이를 통해, 특정 생성자에 `@Builder`를 사용하면, 생성자의 파라미터만 가지고 빌더패턴을 만들 수 있음을 확인.

**코드**
```java
public class ConstructLevelDto2 {
    private String name;
    private int age;

    @Builder
    public ConstructLevelDto2(String name) {
        this.name = name;
        this.age = 1;
    }
}
```

**컴파일**
```java
public class ConstructLevelDto2 {
    private String name;
    private int age;

    public ConstructLevelDto2(String name) {
        this.name = name;
        this.age = 1;
    }

    public static ConstructLevelDto2Builder builder() {
        return new ConstructLevelDto2Builder();
    }

    public static class ConstructLevelDto2Builder {
        private String name;

        ConstructLevelDto2Builder() {
        }

        public ConstructLevelDto2Builder name(final String name) {
            this.name = name;
            return this;
        }

        public ConstructLevelDto2 build() {
            return new ConstructLevelDto2(this.name);
        }

        public String toString() {
            return "ConstructLevelDto2.ConstructLevelDto2Builder(name=" + this.name + ")";
        }
    }
}
```

### 예시 3

- final 맴버를 가진 Dto 형태의 클래스
- 어차피 `build()` 호출 시 생성자를 사용하기 때문에 상관 없음.
- 마찬가지로, `final`의 키워드 취지와는 다소 맞지 않을 수 있음.

**코드**
```java
public class ConstructLevelDto3 {
    private final String name;
    private int age;

    @Builder
    public ConstructLevelDto3(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
```

**컴파일**
```java
public class ConstructLevelDto3 {
    private final String name;
    private int age;

    public ConstructLevelDto3(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public static ConstructLevelDto3Builder builder() {
        return new ConstructLevelDto3Builder();
    }

    public static class ConstructLevelDto3Builder {
        private String name;
        private int age;

        ConstructLevelDto3Builder() {
        }

        public ConstructLevelDto3Builder name(final String name) {
            this.name = name;
            return this;
        }

        public ConstructLevelDto3Builder age(final int age) {
            this.age = age;
            return this;
        }

        public ConstructLevelDto3 build() {
            return new ConstructLevelDto3(this.name, this.age);
        }

        public String toString() {
            return "ConstructLevelDto3.ConstructLevelDto3Builder(name=" + this.name + ", age=" + this.age + ")";
        }
    }
}
```

### 예시 4

- final 맴버가 초기화되어있는 클래스
- 이와 별개로 생성자에 `@Builder` 사용 시, 해당 생성자를 사용하여 빌더 패턴을 생성함을 확인함.

**코드**
```java
public class ConstructLevelDto4 {
    private final String name = "홍길동";
    private int age;

    @Builder
    public ConstructLevelDto4(int age) {
        this.age = age;
    }
}
```

**컴파일**
```java
public class ConstructLevelDto4 {
    private final String name = "홍길동";
    private int age;

    public ConstructLevelDto4(int age) {
        this.age = age;
    }

    public static ConstructLevelDto4Builder builder() {
        return new ConstructLevelDto4Builder();
    }

    public static class ConstructLevelDto4Builder {
        private int age;

        ConstructLevelDto4Builder() {
        }

        public ConstructLevelDto4Builder age(final int age) {
            this.age = age;
            return this;
        }

        public ConstructLevelDto4 build() {
            return new ConstructLevelDto4(this.age);
        }

        public String toString() {
            return "ConstructLevelDto4.ConstructLevelDto4Builder(age=" + this.age + ")";
        }
    }
}
```