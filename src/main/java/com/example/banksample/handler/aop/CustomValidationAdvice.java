package com.example.banksample.handler.aop;

import com.example.banksample.dto.ResponseDTO;
import com.example.banksample.handler.exception.CustomValidationException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;

/**
 * 유효성 검사는 body 가 존재하는 곳에서만 진행한다.
 */
@Component
@Aspect
public class CustomValidationAdvice {

    // PostMapping 에 관한 조인포인트
    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void postMapping() {
    }

    // PutMapping 에 관한 조인포인트
    @Pointcut("@annotation(org.springframework.web.bind.annotation.PutMapping)")
    public void putMapping() {
    }


    /**
     * Advice 에 사용할 수 있는 어노테이션은 다음과 같다.
     *
     * @Before: JoinPoint 이전에 호출
     * @After: JoinPoint 이후 호출
     * @Around: JoinPoint 이전, 이후 호출
     * @AfterThrowing: JoinPoint 가 예외를 던지는 경우 호출
     * @AfterReturing: 메서드가 성공적으로 실행되는 경우 호출
     */
    @Around("postMapping() || putMapping()")
    public Object validationAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();

        for (Object arg : args) {
            if (arg instanceof BindingResult) {
                BindingResult bindingResult = (BindingResult) arg;
                if (bindingResult.hasErrors()) {
                    Map<String, String> errorMap = new HashMap<>();
                    for (FieldError fieldError : bindingResult.getFieldErrors()) {
                        errorMap.put(fieldError.getField(), fieldError.getDefaultMessage());
                    }
                    throw new CustomValidationException("유효성 검사에 실패했습니다.", errorMap);
                }
            }
        }

        // 메서드(=조인 포인트)를 실행시킨다.
        return pjp.proceed();
    }
}
