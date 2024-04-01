package com.example.validate.custom;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordValidator.class) // 어떤 클래스를 사용해서 검증 할지
@Documented
public @interface Password {

    String message() default "패스워드 형태가 올바르지 않습니다."; // 검증 실패 시 반환할 메시지

    Class<?>[] groups() default {}; // 유효성 검증이 진행될 그룹

    Class<? extends Payload>[] payload() default {}; // 유효성 검증 시 전달할 메타 정보
}
