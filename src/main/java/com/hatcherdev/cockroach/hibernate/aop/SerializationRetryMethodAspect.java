package com.hatcherdev.cockroach.hibernate.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import org.hibernate.exception.GenericJDBCException;
import javax.persistence.PersistenceException;

@Aspect
public class SerializationRetryMethodAspect {

    @Pointcut("@annotation(serializationRetry)")
    public void callAt(SerializationRetry serializationRetry) {
    }

    @Around(value = "@annotation(serializationRetry)", argNames = "serializationRetry")
    public Object around(ProceedingJoinPoint pjp, SerializationRetry serializationRetry) throws Throwable {
        Integer retryCount = serializationRetry.retryCount();
        Integer retryCounter = 0;
        Object result = null;
        while (retryCounter < retryCount) {
            try {
                result = pjp.proceed();
                break;
            } catch (PersistenceException exception) {
                retryCounter = handleException(exception, retryCounter, retryCount);
            }
        }
        return result;
    }

    private Integer handleException(PersistenceException exception, Integer retryCounter, Integer retryCount) {
        if (isSerializationError(exception)) {
            retryCounter++;
            if (retryCounter == (retryCount - 1)) {
                throw exception;
            }
        } else {
            throw exception;
        }
        return retryCounter;
    }

    private boolean isSerializationError(PersistenceException exception) {
        boolean isSerializationError = false;
        if (exception.getCause() instanceof GenericJDBCException) {
            GenericJDBCException jdbcException = (GenericJDBCException) exception;
            if (jdbcException.getSQLException().getErrorCode() == 40001) {
                isSerializationError = true;
            }
        }
        return isSerializationError;
    }

}
