package com.example.validate.custom;

import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

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
