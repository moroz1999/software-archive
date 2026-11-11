package com.archive.backend.shared.web;

import com.archive.backend.shared.domain.error.BusinessException;
import com.archive.backend.shared.domain.error.ConflictException;
import com.archive.backend.shared.domain.error.InvalidValueException;
import com.archive.backend.shared.domain.error.NotFoundException;
import com.archive.backend.shared.domain.error.RuleViolationException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * The only place that turns exceptions into REST statuses and ProblemDetail bodies (docs/error-handling.md).
 * Spring MVC's own exceptions (malformed body, wrong method, …) are handled by the base class.
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    ProblemDetail notFound(NotFoundException e) {
        return business(HttpStatus.NOT_FOUND, e);
    }

    @ExceptionHandler(ConflictException.class)
    ProblemDetail conflict(ConflictException e) {
        return business(HttpStatus.CONFLICT, e);
    }

    /**
     * A change that lost to another one running at the same time, such as a deadlock between them: repeating it after
     * reloading succeeds.
     */
    @ExceptionHandler(ConcurrencyFailureException.class)
    ProblemDetail concurrent(ConcurrencyFailureException e) {
        log.warn("A change lost to a concurrent one", e);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, "The item was changed by another change at the same time.");
        problem.setProperty("code", "conflict.concurrent");
        return problem;
    }

    @ExceptionHandler(RuleViolationException.class)
    ProblemDetail ruleViolation(RuleViolationException e) {
        ProblemDetail problem = business(HttpStatus.UNPROCESSABLE_ENTITY, e);
        String field = e.field();
        if (field != null) {
            problem.setProperty("errors", List.of(Map.of("field", field, "code", e.code())));
        }
        return problem;
    }

    @ExceptionHandler(InvalidValueException.class)
    ProblemDetail invalidValue(InvalidValueException e) {
        return business(HttpStatus.BAD_REQUEST, e);
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail unexpected(Exception e) {
        log.error("Unhandled exception", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error.");
    }

    private static ProblemDetail business(HttpStatus status, BusinessException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, e.getMessage());
        problem.setProperty("code", e.code());
        return problem;
    }
}
