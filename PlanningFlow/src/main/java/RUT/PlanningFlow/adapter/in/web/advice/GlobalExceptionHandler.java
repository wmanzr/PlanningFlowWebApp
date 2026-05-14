package RUT.PlanningFlow.adapter.in.web.advice;

import RUT.PlanningFlow.adapter.in.web.dto.ErrorResponse;
import RUT.PlanningFlow.domain.exception.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static boolean isAssertTrueFieldError(final FieldError fe) {
        final String[] codes = fe.getCodes();
        if (codes == null) {
            return false;
        }
        return Arrays.stream(codes).anyMatch(c -> c != null && c.contains("AssertTrue"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex) {
        final Map<String, String> fieldErrors = new LinkedHashMap<>();
        final StringBuilder global = new StringBuilder();
        for (final FieldError fe : ex.getBindingResult().getFieldErrors()) {
            final String msg = fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Некорректное значение";
            if (isAssertTrueFieldError(fe)) {
                if (!global.isEmpty()) {
                    global.append(' ');
                }
                global.append(msg);
            } else {
                fieldErrors.put(fe.getField(), msg);
            }
        }
        for (final ObjectError ge : ex.getBindingResult().getGlobalErrors()) {
            if (!global.isEmpty()) {
                global.append(' ');
            }
            global.append(ge.getDefaultMessage() != null ? ge.getDefaultMessage() : "Ошибка валидации");
        }
        if (!global.isEmpty()) {
            fieldErrors.put("_global", global.toString());
        }
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        "Проверьте введенные данные",
                        "VALIDATION_FAILED",
                        System.currentTimeMillis(),
                        fieldErrors
                ));
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomain(DomainException ex) {
        final HttpStatus status = switch (ex.getErrorCode()) {
            case "EXTERNAL_SUPPLIER_UNAVAILABLE", "EXTERNAL_SUPPLIER_TIMEOUT" -> HttpStatus.SERVICE_UNAVAILABLE;
            case "USERNAME_TAKEN", "EMAIL_TAKEN" -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity
                .status(status)
                .body(new ErrorResponse(ex.getMessage(), ex.getErrorCode(), System.currentTimeMillis()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(final BadCredentialsException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Неудачная авторизация", "AUTH_FAILED", System.currentTimeMillis()));
    }
}