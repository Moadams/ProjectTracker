package com.buildmaster.projecttracker.audit;
import com.buildmaster.projecttracker.dto.DeveloperDTO;
import com.buildmaster.projecttracker.dto.ProjectDTO;
import com.buildmaster.projecttracker.dto.TaskDTO;
import com.buildmaster.projecttracker.enums.ActionType;
import com.buildmaster.projecttracker.enums.EntityType;
import com.buildmaster.projecttracker.service.AuditLogService;
import com.buildmaster.projecttracker.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Arrays;

/**
 * Aspect for auditing method executions annotated with @Auditable.
 * It captures information about method calls, return values, and exceptions
 * to log them via the AuditLogService.
 */
@Aspect
@Component
@RequiredArgsConstructor
public class AuditingAspect {

    private static final Logger logger = LoggerFactory.getLogger(AuditingAspect.class);

    private final AuditLogService auditLogService;
    private final SecurityUtil securityUtil;

    /**
     * Defines a pointcut for any method annotated with @Auditable.
     * The 'auditable' argument will capture the @Auditable annotation itself.
     */
    @Pointcut("@annotation(auditable)")
    public void auditableMethod(Auditable auditable) {}


    /**
     * Advice that runs after a method annotated with @Auditable returns successfully.
     * It captures details about the method execution and logs a success event.
     *
     * @param joinPoint The join point providing method execution details.
     * @param auditable The @Auditable annotation instance.
     * @param result The return value of the method.
     */
    @AfterReturning(pointcut = "auditableMethod(auditable)", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Auditable auditable, Object result) {
        String currentUserEmail = securityUtil.getCurrentUserEmail();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();


        EntityType entityType = auditable.entityType();

        String entityId = null;

        if (result != null) {
            entityId = extractEntityId(result);
        }

        Object[] args = joinPoint.getArgs();
        String formattedMessage = MessageFormat.format(auditable.message(), args);

        auditLogService.logAudit(
                auditable.action(),
                entityType,
                entityId,
                formattedMessage,
                currentUserEmail
        );
        logger.debug("Audited successful method execution: {}.{} - Action: {}", className, methodName, auditable.action());
    }

    /**
     * Advice that runs if a method annotated with @Auditable throws an exception.
     * It logs a failure event.
     *
     * @param joinPoint The join point providing method execution details.
     * @param auditable The @Auditable annotation instance.
     * @param ex The exception that was thrown.
     */
    @AfterThrowing(pointcut = "auditableMethod(auditable)", throwing = "ex")
    public void doAfterThrowing(JoinPoint joinPoint, Auditable auditable, Throwable ex) {
        String currentUserEmail = securityUtil.getCurrentUserEmail();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        EntityType entityType = auditable.entityType();

        Long entityId = null;
        if (joinPoint.getArgs().length > 0 && joinPoint.getArgs()[0] instanceof Long) {
            entityId = (Long) joinPoint.getArgs()[0];
        }

        Object[] args = joinPoint.getArgs();
        String formattedMessage = MessageFormat.format(auditable.message() + " (Failed: {0})",
                Arrays.copyOf(args, args.length + 1)); // Add space for exception message
        formattedMessage = MessageFormat.format(formattedMessage, ex.getMessage());

        auditLogService.logAudit(
                ActionType.GENERIC_FAILURE,
                entityType,
                entityId.toString(),
                formattedMessage,
                currentUserEmail
        );
        logger.error("Audited failed method execution: {}.{} - Action: {} - Error: {}", className, methodName, auditable.action(), ex.getMessage());
    }

    private String extractEntityId(Object result) {
        try {

            if (result instanceof ProjectDTO.ProjectSummaryResponse) {
                return ((ProjectDTO.ProjectSummaryResponse) result).id().toString();
            } else if (result instanceof DeveloperDTO.DeveloperResponse) {
                return ((DeveloperDTO.DeveloperResponse) result).id().toString();
            } else if (result instanceof TaskDTO.TaskResponse) {
                return ((TaskDTO.TaskResponse) result).id().toString();
            }
        } catch (Exception e) {
            logger.debug("Could not extract entity ID from method return result: {}", e.getMessage());
        }finally {
            return "unknown";
        }
    }

}
