package dev.relaydesk.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpStatus.FORBIDDEN.value());

        Map<String, Object> body = new HashMap<>();
        body.put("code", "FORBIDDEN");
        body.put("message", accessDeniedException.getMessage());
        body.put("fieldErrors", new Object[0]);
        body.put("traceId", request.getAttribute("traceId"));

        mapper.writeValue(response.getOutputStream(), body);
    }
}
