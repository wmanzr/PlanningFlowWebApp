package RUT.PlanningFlow.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String message,
        String errorCode,
        long timestamp,
        Map<String, String> fieldErrors
) {
    public ErrorResponse(final String message, final String errorCode, final long timestamp) {
        this(message, errorCode, timestamp, null);
    }
}