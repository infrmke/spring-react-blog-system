package br.com.spring_react.blog.infra.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /* EXCEÇÕES GENÉRICAS */

    // lida com ROTAS QUE NÃO EXISTEM
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoHandlerFoundException ex,
                                                        HttpServletRequest request) {
        return ErrorResponse.build(
                "about:blank",
                HttpStatus.NOT_FOUND,
                "Route not found",
                request.getRequestURI(),
                null
        );
    }

    // lida com ERRO DE DUPLICIDADE no banco de dados
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleConflict(DataIntegrityViolationException ex,
                                                        HttpServletRequest request) {
        return ErrorResponse.build(
                "about:blank",
                HttpStatus.CONFLICT,
                "One or more records already exist",
                request.getRequestURI(),
                null
        );
    }

    // lida com ERRO DE CONVERSÃO de parâmetros (ex: UUID)
    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        // pega o tipo esperado ou usa um valor genérico
        String requiredType = (ex.getRequiredType() != null)
                ? ex.getRequiredType().getSimpleName()
                : "defined type";

        String detail = String.format("The parameter '%s' has an invalid value. Expected type: %s",
                ex.getName(), requiredType);

        return ErrorResponse.build(
                "about:blank",
                HttpStatus.BAD_REQUEST,
                detail,
                request.getRequestURI(),
                null
        );
    }

    // lida com ERROS DE VALIDAÇÃO no estilo Zod
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<ValidationErrorResponse> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ValidationErrorResponse(error.getField(),
                        error.getDefaultMessage()))
                .toList();

        return ErrorResponse.build(
                "about:blank",
                HttpStatus.BAD_REQUEST,
                "Your request has invalid fields",
                request.getRequestURI(),
                validationErrors
        );
    }

    // lida com RESTRIÇÃO DE ACESSO
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex,
                                                            HttpServletRequest request) {
        return ErrorResponse.build(
                "about:blank",
                HttpStatus.FORBIDDEN,
                "You are not authorized to proceed with this action",
                request.getRequestURI(),
                null
        );
    }

    // fallback pra qualquer outro erro (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex, HttpServletRequest request) {
        return ErrorResponse.build(
                "about:blank",
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    /* EXCEÇÕES PERSONALIZADAS */

    // lida com RESTRIÇÃO DE ACESSO
    @ExceptionHandler(ForbiddenActionException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenAction(ForbiddenActionException ex,
                                                               HttpServletRequest request) {
        return ErrorResponse.build(
                "about:blank",
                HttpStatus.FORBIDDEN,
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    // lida com RECURSO NÃO ENCONTRADO
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        return ErrorResponse.build(
                "about:blank",
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    // lida com DUPLICIDADE DE RECURSO
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleResourceAlreadyExists(
            ResourceAlreadyExistsException ex, HttpServletRequest request) {
        return ErrorResponse.build(
                "about:blank",
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    // lida com EXCEDÊNCIA DE REQUISIÇÕES
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenAction(RateLimitExceededException ex,
                                                               HttpServletRequest request) {
        return ErrorResponse.build(
                "about:blank",
                HttpStatus.TOO_MANY_REQUESTS,
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
    }
}
