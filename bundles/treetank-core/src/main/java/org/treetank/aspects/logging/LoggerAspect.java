package org.treetank.aspects.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class LoggerAspect {

    public static final ILoggerFactory FACTORY = LoggerFactory.getILoggerFactory();

    @Around("@annotation(org.treetank.aspects.logging.Logging)")
    public Object advice(ProceedingJoinPoint pjp) throws Throwable {
        final Signature sig = pjp.getSignature();
        final Logger LOGGER = FACTORY.getLogger(sig.getDeclaringTypeName());
        LOGGER.debug(new StringBuilder("Entering ").append(sig.getDeclaringTypeName()).toString());
        Object returnVal = pjp.proceed();
        LOGGER.debug(new StringBuilder("Exiting ").append(sig.getDeclaringTypeName()).toString());
        return returnVal;
    }

}
