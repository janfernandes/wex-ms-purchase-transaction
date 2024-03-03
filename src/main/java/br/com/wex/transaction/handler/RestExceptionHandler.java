package br.com.wex.transaction.handler;

import br.com.wex.transaction.exception.PurchaseTransactionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Controller Advisory.
 */
@Slf4j
@RestControllerAdvice
public class RestExceptionHandler {

    private final MessageSource messageSource;

    @Autowired
    public RestExceptionHandler(final MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public record RestErrorResponse(int status, String message, LocalDateTime timestamp) {

    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    RestErrorResponse handleException(final HttpMessageNotReadableException ex) {
        log.error("Cannot parse request.", ex);
        return new RestErrorResponse(HttpStatus.NOT_ACCEPTABLE.value(), ex.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    RestErrorResponse handleException(final MethodArgumentNotValidException ex, final Locale locale) {
        log.error("Cannot parse request.", ex);
        String message = ex.getFieldErrors()
            .stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .reduce(messageSource.getMessage("errors.found", null, locale), (subErrorDescription, elementDescription) -> {
                assert elementDescription != null;
                return subErrorDescription.concat(elementDescription).concat(" ");
            })
            .replaceAll("\\s+$", "");
        return new RestErrorResponse(HttpStatus.BAD_REQUEST.value(), message, LocalDateTime.now());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    RestErrorResponse handleMissingServletRequestParameter(final MissingServletRequestParameterException ex,
        final Locale locale) {
        log.error("Cannot parse request.", ex);
        String message = messageSource.getMessage("exception.requiredParam", new String[] { ex.getParameterName() }, locale);
        return new RestErrorResponse(HttpStatus.BAD_REQUEST.value(), message, LocalDateTime.now());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    RestErrorResponse handleException(final MethodArgumentTypeMismatchException ex, final Locale locale) {
        log.error("Cannot parse request.", ex);
        return new RestErrorResponse(HttpStatus.BAD_REQUEST.value(),
            messageSource.getMessage("exception.mismatch", new Object[] { ex.getValue(), ex.getRequiredType().getSimpleName() },
                locale), LocalDateTime.now());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    RestErrorResponse handleException(final IllegalArgumentException ex) {
        log.error("Transaction not found.", ex);
        return new RestErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler(PurchaseTransactionException.class)
    @ResponseStatus(HttpStatus.PRECONDITION_FAILED)
    public RestErrorResponse handleException(final PurchaseTransactionException ex) {
        log.error("Currency within 6 months not found.", ex);
        return new RestErrorResponse(HttpStatus.PRECONDITION_FAILED.value(), ex.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    RestErrorResponse handleException(final Exception ex) {
        log.error("An unexpected error occurred", ex);
        return new RestErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), LocalDateTime.now());
    }
}
