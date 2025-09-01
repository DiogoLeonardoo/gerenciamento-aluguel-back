package com.inhouse.project.exceptions;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

        /**
         * Estrutura de resposta de erro padrão
         */
        private Map<String, Object> buildErrorResponse(HttpStatus status, String message, String path) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("timestamp", LocalDateTime.now().toString());
                errorResponse.put("status", status.value());
                errorResponse.put("error", status.getReasonPhrase());
                errorResponse.put("message", message);
                errorResponse.put("path", path);
                return errorResponse;
        }

        /**
         * Trata exceções de regra de negócio (BusinessException)
         */
        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<Object> handleBusinessException(BusinessException ex, WebRequest request) {
                log.error("Business error: {}", ex.getMessage(), ex);

                Map<String, Object> body = buildErrorResponse(
                        HttpStatus.BAD_REQUEST,
                        ex.getMessage(),
                        request.getDescription(false).replace("uri=", ""));

                return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }

        /**
         * Trata exceções de recursos não encontrados (ResourceNotFoundException)
         */
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex,
                        WebRequest request) {
                log.error("Resource not found: {}", ex.getMessage(), ex);
                Map<String, Object> body = buildErrorResponse(
                                HttpStatus.NOT_FOUND,
                                ex.getMessage(),
                                request.getDescription(false).replace("uri=", ""));
                return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
        }

        /**
         * Trata exceções de validação de argumentos de métodos
         */
        @Override
        protected ResponseEntity<Object> handleMethodArgumentNotValid(
                        MethodArgumentNotValidException ex,
                        HttpHeaders headers,
                        HttpStatusCode status,
                        WebRequest request) {

                log.error("Validation error: {}", ex.getMessage());

                // Coleta todos os erros de validação em um mapa
                Map<String, String> validationErrors = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .collect(Collectors.toMap(
                                                FieldError::getField,
                                                error -> error.getDefaultMessage() != null ? error.getDefaultMessage()
                                                                : "Erro de validação"));

                Map<String, Object> body = buildErrorResponse(
                                HttpStatus.BAD_REQUEST,
                                "Erro de validação dos dados",
                                request.getDescription(false).replace("uri=", ""));
                body.put("errors", validationErrors);

                return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }

        /**
         * Trata exceções de violação de restrições (validações)
         */
        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<Object> handleConstraintViolation(
                        ConstraintViolationException ex,
                        WebRequest request) {

                log.error("Constraint violation: {}", ex.getMessage());

                // Coleta todas as violações em um mapa
                Map<String, String> validationErrors = ex.getConstraintViolations()
                                .stream()
                                .collect(Collectors.toMap(
                                                violation -> violation.getPropertyPath().toString(),
                                                ConstraintViolation::getMessage));

                Map<String, Object> body = buildErrorResponse(
                                HttpStatus.BAD_REQUEST,
                                "Erro de validação dos dados",
                                request.getDescription(false).replace("uri=", ""));
                body.put("errors", validationErrors);

                return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }

        /**
         * Trata exceções de acesso negado (segurança)
         */
        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<Object> handleAccessDeniedException(
                        AccessDeniedException ex,
                        WebRequest request) {

                log.error("Access denied: {}", ex.getMessage());

                Map<String, Object> body = buildErrorResponse(
                                HttpStatus.FORBIDDEN,
                                "Acesso negado: Você não tem permissão para acessar este recurso",
                                request.getDescription(false).replace("uri=", ""));

                return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
        }

        /**
         * Trata exceções de autenticação
         */
        @ExceptionHandler({ AuthenticationException.class, BadCredentialsException.class })
        public ResponseEntity<Object> handleAuthenticationException(
                        Exception ex,
                        WebRequest request) {

                log.error("Authentication error: {}", ex.getMessage());

                Map<String, Object> body = buildErrorResponse(
                                HttpStatus.UNAUTHORIZED,
                                "Erro de autenticação: " + ex.getMessage(),
                                request.getDescription(false).replace("uri=", ""));

                return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
        }

        /**
         * Trata exceções genéricas não tratadas em outros handlers
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<Object> handleAllOtherExceptions(
                        Exception ex,
                        WebRequest request) {

                log.error("Unexpected error: {}", ex.getMessage(), ex);

                Map<String, Object> body = buildErrorResponse(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "Ocorreu um erro interno no servidor. Por favor, tente novamente mais tarde.",
                                request.getDescription(false).replace("uri=", ""));

                // Em ambiente de desenvolvimento, adicionar a pilha de exceção completa para
                // debugging
                if (!isProduction()) {
                        body.put("debug", ex.getMessage());
                        body.put("exception", ex.getClass().getName());
                        body.put("trace", ex.getStackTrace()[0].toString());
                }

                return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        /**
         * Verifica se o ambiente é de produção
         */
        private boolean isProduction() {
                String profile = System.getProperty("spring.profiles.active", "dev");
                return "prod".equals(profile);
        }
}
