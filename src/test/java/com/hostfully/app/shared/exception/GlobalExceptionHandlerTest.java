package com.hostfully.app.shared.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.hostfully.app.block.exceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private HttpServletRequest request;

    @Mock
    private BindingResult bindingResult;

    private static final String TEST_REQUEST_URI = "/api/test";

    @BeforeEach
    void setUp() {
        when(request.getRequestURI()).thenReturn(TEST_REQUEST_URI);
    }

    public void dummyMethod(final String param) {
        // This method is only used for creating MethodParameter in tests
    }

    @Test
    @DisplayName("should handle MethodArgumentNotValidException and return validation errors")
    void shouldHandleMethodArgumentNotValidException() throws NoSuchMethodException {
        final FieldError fieldError1 = new FieldError("objectName", "fieldName1", "must not be null");
        final FieldError fieldError2 = new FieldError("objectName", "fieldName2", "must be valid");
        final List<FieldError> fieldErrors = List.of(fieldError1, fieldError2);

        final Method method = this.getClass().getMethod("dummyMethod", String.class);
        final MethodParameter methodParameter = new MethodParameter(method, 0);
        final MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(methodParameter, bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.copyOf(fieldErrors));

        final ResponseEntity<ProblemDetail> response =
                globalExceptionHandler.handleValidationException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();

        final ProblemDetail problemDetail = response.getBody();
        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Validation Error");
        assertThat(problemDetail.getDetail()).isEqualTo("Validation failed for one or more fields");
        assertThat(problemDetail.getInstance()).isEqualTo(URI.create(TEST_REQUEST_URI));
        assertThat(problemDetail.getType()).isEqualTo(URI.create("about:blank"));
        assertThat(problemDetail.getProperties()).containsKey("timestamp");
        assertThat(problemDetail.getProperties()).containsKey("errors");

        @SuppressWarnings("unchecked")
        final Map<String, String> errors =
                (Map<String, String>) problemDetail.getProperties().get("errors");
        assertThat(errors).hasSize(2);
        assertThat(errors).containsEntry("fieldName1", "must not be null");
        assertThat(errors).containsEntry("fieldName2", "must be valid");
    }

    @Test
    @DisplayName("Should handle generic Exception and return internal server error")
    void shouldHandleGenericException() {
        final Exception exception = new Exception("Unexpected error occurred");

        final ResponseEntity<ProblemDetail> response =
                globalExceptionHandler.handleGenericException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();

        final ProblemDetail problemDetail = response.getBody();
        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Internal Server Error");
        assertThat(problemDetail.getDetail()).isEqualTo("An unexpected error occurred");
        assertThat(problemDetail.getInstance()).isEqualTo(URI.create(TEST_REQUEST_URI));
        assertThat(problemDetail.getType()).isEqualTo(URI.create("about:blank"));
        assertThat(problemDetail.getProperties()).containsKey("timestamp");
    }

    @Test
    @DisplayName("Should handle BlockCreationException and return internal server error")
    void shouldHandleBlockCreationException() {
        final BlockGenericException exception = new BlockGenericException("Failed to create block", null);

        final ResponseEntity<ProblemDetail> response =
                globalExceptionHandler.handleBlockCreationException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();

        final ProblemDetail problemDetail = response.getBody();
        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(problemDetail.getTitle()).isEqualTo(exception.getTitle());
        assertThat(problemDetail.getDetail()).isEqualTo("Failed to create block");
        assertThat(problemDetail.getInstance()).isEqualTo(URI.create(TEST_REQUEST_URI));
        assertThat(problemDetail.getType()).isEqualTo(URI.create("about:blank"));
        assertThat(problemDetail.getProperties()).containsKey("timestamp");
    }

    @Test
    @DisplayName("Should handle OverlapBlockException and return conflict status")
    void shouldHandleOverlapBlockException() {
        final OverlapBlockException exception = new OverlapBlockException("Block overlaps with existing block");

        final ResponseEntity<ProblemDetail> response =
                globalExceptionHandler.handleOverlapBlockException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();

        final ProblemDetail problemDetail = response.getBody();
        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(problemDetail.getTitle()).isEqualTo(exception.getTitle());
        assertThat(problemDetail.getDetail()).isEqualTo("Block overlaps with existing block");
        assertThat(problemDetail.getInstance()).isEqualTo(URI.create(TEST_REQUEST_URI));
        assertThat(problemDetail.getType()).isEqualTo(URI.create("about:blank"));
        assertThat(problemDetail.getProperties()).containsKey("timestamp");
    }

    @Test
    @DisplayName("Should handle PropertyNotFoundException and return not found status")
    void shouldHandlePropertyNotFoundException() {
        final PropertyNotFoundException exception = new PropertyNotFoundException("Property not found");

        final ResponseEntity<ProblemDetail> response =
                globalExceptionHandler.handlePropertyNotFoundException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();

        final ProblemDetail problemDetail = response.getBody();
        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(problemDetail.getTitle()).isEqualTo(exception.getTitle());
        assertThat(problemDetail.getDetail()).isEqualTo("Property not found");
        assertThat(problemDetail.getInstance()).isEqualTo(URI.create(TEST_REQUEST_URI));
        assertThat(problemDetail.getType()).isEqualTo(URI.create("about:blank"));
        assertThat(problemDetail.getProperties()).containsKey("timestamp");
    }

    @Test
    @DisplayName("Should handle InvalidDateRangeException and return bad request status")
    void shouldHandleInvalidDateRangeException() {
        final InvalidDateRangeException exception = new InvalidDateRangeException("Start date must be before end date");

        final ResponseEntity<ProblemDetail> response =
                globalExceptionHandler.handleInvalidDateRangeException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();

        final ProblemDetail problemDetail = response.getBody();
        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problemDetail.getTitle()).isEqualTo(exception.getTitle());
        assertThat(problemDetail.getDetail()).isEqualTo("Start date must be before end date");
        assertThat(problemDetail.getInstance()).isEqualTo(URI.create(TEST_REQUEST_URI));
        assertThat(problemDetail.getType()).isEqualTo(URI.create("about:blank"));
        assertThat(problemDetail.getProperties()).containsKey("timestamp");
    }

    @Test
    @DisplayName("Should handle MissingRequestHeaderException and return bad request status")
    void shouldMissingRequestHeaderException() throws NoSuchMethodException {
        final Method method = this.getClass().getMethod("dummyMethod", String.class);
        final MethodParameter methodParameter = new MethodParameter(method, 0);
        final MissingRequestHeaderException exception = new MissingRequestHeaderException("a-header", methodParameter);

        final ResponseEntity<ProblemDetail> response =
                globalExceptionHandler.handleMissingRequestHeaderException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();

        final ProblemDetail problemDetail = response.getBody();
        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Missing mandatory header");
        assertThat(problemDetail.getDetail())
                .isEqualTo("Required request header 'a-header' for method parameter type String is not present");
        assertThat(problemDetail.getInstance()).isEqualTo(URI.create(TEST_REQUEST_URI));
        assertThat(problemDetail.getType()).isEqualTo(URI.create("about:blank"));
        assertThat(problemDetail.getProperties()).containsKey("timestamp");
    }

    @Test
    @DisplayName("Should handle BlockNotFoundException and return bad request status")
    void shouldHandleBlockNotFoundException() {
        final BlockNotFoundException exception = new BlockNotFoundException("Block not found by id provided");

        final ResponseEntity<ProblemDetail> response =
                globalExceptionHandler.handleBlockNotFoundException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();

        final ProblemDetail problemDetail = response.getBody();
        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(problemDetail.getTitle()).isEqualTo(exception.getTitle());
        assertThat(problemDetail.getDetail()).isEqualTo("Block not found by id provided");
        assertThat(problemDetail.getInstance()).isEqualTo(URI.create(TEST_REQUEST_URI));
        assertThat(problemDetail.getType()).isEqualTo(URI.create("about:blank"));
        assertThat(problemDetail.getProperties()).containsKey("timestamp");
    }
}
