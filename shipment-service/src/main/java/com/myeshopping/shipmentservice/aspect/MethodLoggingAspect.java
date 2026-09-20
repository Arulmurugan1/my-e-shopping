package com.myeshopping.shipmentservice.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.StringJoiner;
import java.util.UUID;

@Aspect
@Component
public class MethodLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger("method-trace");

    @Around("(within(@org.springframework.stereotype.Service *) || within(@org.springframework.web.bind.annotation.RestController *))"
            + " && !within(com.myeshopping..JwtService) && !within(com.myeshopping..CustomUserDetailsService)")
    public Object logMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String method = signature.getDeclaringType().getSimpleName() + "." + signature.getName();
        String args = safeArgs(joinPoint.getArgs());
        long start = System.currentTimeMillis();
        log.info("{} started {}", method, args);
        try {
            Object result = joinPoint.proceed();
            log.info("{} completed in {} ms", method, System.currentTimeMillis() - start);
            return result;
        } catch (Throwable ex) {
            log.warn("{} failed in {} ms: {}: {}", method, System.currentTimeMillis() - start, ex.getClass().getSimpleName(), ex.getMessage());
            throw ex;
        }
    }

    private String safeArgs(Object[] args) {
        StringJoiner joiner = new StringJoiner(", ", "[", "]");
        for (Object arg : args) {
            if (arg instanceof Number || arg instanceof Boolean || arg instanceof Enum<?> || arg instanceof UUID) {
                joiner.add(String.valueOf(arg));
            } else if (arg != null) {
                joiner.add(arg.getClass().getSimpleName());
            }
        }
        return joiner.toString();
    }
}