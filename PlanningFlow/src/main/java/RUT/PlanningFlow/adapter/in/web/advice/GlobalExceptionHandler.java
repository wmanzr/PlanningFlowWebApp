package RUT.PlanningFlow.adapter.in.web.advice;

import RUT.PlanningFlow.adapter.in.web.dto.ErrorResponse;
import RUT.PlanningFlow.domain.exception.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
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
        return errorResponse(
                HttpStatus.BAD_REQUEST,
                "Проверьте введенные данные",
                "VALIDATION_FAILED",
                fieldErrors
        );
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomain(DomainException ex) {
        final HttpStatus status = switch (ex.getErrorCode()) {
            case "EXTERNAL_SUPPLIER_UNAVAILABLE", "EXTERNAL_SUPPLIER_TIMEOUT" -> HttpStatus.SERVICE_UNAVAILABLE;
            case "USERNAME_TAKEN", "EMAIL_TAKEN" -> HttpStatus.CONFLICT;
            case "INVALID_CREDENTIALS", "REFRESH_TOKEN_INVALID" -> HttpStatus.UNAUTHORIZED;
            default -> HttpStatus.BAD_REQUEST;
        };
        return errorResponse(status, ex.getMessage(), ex.getErrorCode());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(final ResponseStatusException ex) {
        final HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        final HttpStatus resolved = status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR;
        final String reason = ex.getReason();
        final String message = reason != null && !reason.isBlank() ? reason : defaultMessage(resolved);
        return errorResponse(resolved, message, errorCode(resolved));
    }

    private static ResponseEntity<ErrorResponse> errorResponse(
            final HttpStatus status,
            final String message,
            final String errorCode
    ) {
        return errorResponse(status, message, errorCode, null);
    }

    private static ResponseEntity<ErrorResponse> errorResponse(
            final HttpStatus status,
            final String message,
            final String errorCode,
            final Map<String, String> fieldErrors
    ) {
        return ResponseEntity
                .status(status)
                .body(new ErrorResponse(message, errorCode, System.currentTimeMillis(), fieldErrors));
    }

    private static String errorCode(final HttpStatus status) {
        return switch (status) {
            case NOT_FOUND -> "NOT_FOUND";
            case FORBIDDEN -> "ACCESS_DENIED";
            case UNAUTHORIZED -> "UNAUTHORIZED";
            case CONFLICT -> "CONFLICT";
            case SERVICE_UNAVAILABLE -> "SERVICE_UNAVAILABLE";
            case BAD_REQUEST -> "BAD_REQUEST";
            default -> "HTTP_" + status.value();
        };
    }

    private static String defaultMessage(final HttpStatus status) {
        return switch (status) {
            case NOT_FOUND -> "Ресурс не найден";
            case FORBIDDEN -> "Доступ запрещён";
            case UNAUTHORIZED -> "Требуется авторизация";
            case CONFLICT -> "Конфликт данных";
            case SERVICE_UNAVAILABLE -> "Сервис временно недоступен";
            case BAD_REQUEST -> "Некорректный запрос";
            default -> "Ошибка запроса";
        };
    }

    private static boolean isAssertTrueFieldError(final FieldError fe) {
        final String[] codes = fe.getCodes();
        if (codes == null) {
            return false;
        }
        return Arrays.stream(codes).anyMatch(c -> c != null && c.contains("AssertTrue"));
    }
}