package br.com.spring_react.blog.infra.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // lida com ROTAS QUE NÃO EXISTEM
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoHandlerFoundException ex) {
        return ErrorResponse.build(HttpStatus.NOT_FOUND, "Route not found.", "ROUTE_NOT_FOUND",
                null);
    }

    // lida com ERROS DE VALIDAÇÃO (exibe apenas o primeiro)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ErrorResponse.build(HttpStatus.BAD_REQUEST, msg, "VALIDATION_ERROR", null);
    }

    /* EXCEÇÕES PERSONALIZADAS */

    // lida com RESTRIÇÃO DE ACESSO
    @ExceptionHandler(ForbiddenActionException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenAction(ForbiddenActionException ex) {
        return ErrorResponse.build(HttpStatus.FORBIDDEN, ex.getMessage(), "FORBIDDEN_ACTION", ex);
    }

    // lida com RECURSO NÃO ENCONTRADO
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        return ErrorResponse.build(HttpStatus.NOT_FOUND, ex.getMessage(), "RESOURCE_NOT_FOUND", ex);
    }

    // lida com DUPLICIDADE DE RECURSO
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleResourceAlreadyExists(ResourceAlreadyExistsException ex) {
        return ErrorResponse.build(HttpStatus.CONFLICT, ex.getMessage(), "RESOURCE_ALREADY_EXISTS"
                , ex);
    }

    // fallback pra qualquer outro erro (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex) {
        return ErrorResponse.build(HttpStatus.INTERNAL_SERVER_ERROR, "There was an unexpected " +
                "error. " +
                "Try again later.", "INTERNAL_SERVER_ERROR", ex);
    }
}
