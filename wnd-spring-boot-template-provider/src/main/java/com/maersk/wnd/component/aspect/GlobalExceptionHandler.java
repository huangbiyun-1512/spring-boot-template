package com.maersk.wnd.component.aspect;

import com.maersk.commons.component.dto.BaseErrorDto;
import com.maersk.commons.component.dto.BaseResponseDto;
import com.maersk.commons.component.exception.BusinessException;
import com.maersk.commons.component.util.ErrorUtil;
import com.maersk.wnd.component.constant.MessageConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.stream.Collectors.toList;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  private final ErrorUtil errorUtil;

  public GlobalExceptionHandler(ErrorUtil errorUtil) {
    this.errorUtil = errorUtil;
  }

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity handleBusinessException(BusinessException e) {
    log.error("BusinessException: ", e);
    return buildResponseEntity(e.getErrors());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity handleValidationException(MethodArgumentNotValidException e) {
    log.error("MethodArgumentNotValidException: ", e);
    List<BaseErrorDto> errors =
        e.getBindingResult().getFieldErrors().stream()
            .map(x -> errorUtil.buildError(
                HttpStatus.BAD_REQUEST.value(),
                MessageConstant.MESSAGE_KEY_E01_0003,
                new Object[]{x.getField()}))
            .collect(toList());
    return buildResponseEntity(errors);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity handleMismatchException(MethodArgumentTypeMismatchException e) {
    log.error("MethodArgumentTypeMismatchException: ", e);
    return buildResponseEntity(
        errorUtil.build400ErrorList(
            MessageConstant.MESSAGE_KEY_E01_0003, e.getMessage()));
  }

  @ExceptionHandler(HttpClientErrorException.class)
  public ResponseEntity handleHttpClientErrorException(HttpClientErrorException e) {
    log.error("HttpClientErrorException: ", e);
    return buildResponseEntity(
        errorUtil.build400ErrorList(
            MessageConstant.MESSAGE_KEY_E01_0001, e.getMessage()));
  }

  @ExceptionHandler(HttpServerErrorException.class)
  public ResponseEntity handleHttpServerErrorException(HttpServerErrorException e) {
    log.error("HttpServerErrorException: ", e);
    return buildResponseEntity(
        errorUtil.build500ErrorList(
            MessageConstant.MESSAGE_KEY_E01_0001, e.getMessage()));
  }

  @ExceptionHandler(ResourceAccessException.class)
  public ResponseEntity handleResourceAccessException(ResourceAccessException e) {
    log.error("ResourceAccessException: ", e);
    return buildResponseEntity(
        errorUtil.build500ErrorList(
            MessageConstant.MESSAGE_KEY_E01_0001, e.getMessage()));
  }

  @ExceptionHandler(TimeoutException.class)
  public ResponseEntity handleTimeoutException(TimeoutException e) {
    log.error("TimeoutException: ", e);
    return buildResponseEntity(
        errorUtil.build408ErrorList(
            MessageConstant.MESSAGE_KEY_E01_0002, e.getMessage()));
  }

  @ExceptionHandler(ExecutionException.class)
  public ResponseEntity handleExecutionException(ExecutionException e) {
    log.error("ExecutionException: ", e);
    return handleKindsOfException(e);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity handleException(Exception e) {
    log.error("Exception: ", e);
    return buildResponseEntity(
        errorUtil.build500ErrorList(
            MessageConstant.MESSAGE_KEY_E01_0001, e.getMessage()));
  }

  private ResponseEntity buildResponseEntity(Iterable<? extends BaseErrorDto> errors) {
    int status = HttpStatus.BAD_REQUEST.value();

    if (!Objects.isNull(errors)) {
      for (BaseErrorDto error : errors) {
        status = Integer.valueOf(error.getStatus());
        break;
      }
    }

    BaseResponseDto baseResponseDto =
        BaseResponseDto.builder().errors(errors).build();

    return new ResponseEntity<>(
        baseResponseDto,
        new HttpHeaders(),
        HttpStatus.valueOf(status));
  }

  private ResponseEntity handleKindsOfException(Exception e) {
    if (e.getCause() instanceof BusinessException) {
      return handleBusinessException((BusinessException) e.getCause());
    } else if (e.getCause() instanceof HttpClientErrorException) {
      return handleHttpClientErrorException((HttpClientErrorException) e.getCause());
    } else if (e.getCause() instanceof HttpServerErrorException) {
      return handleHttpServerErrorException((HttpServerErrorException) e.getCause());
    } else if (e.getCause() instanceof ResourceAccessException) {
      return handleResourceAccessException((ResourceAccessException) e.getCause());
    } else if (e.getCause() instanceof TimeoutException) {
      return handleTimeoutException((TimeoutException) e.getCause());
    } else if (e.getCause() instanceof ExecutionException) {
      return handleExecutionException((ExecutionException) e.getCause());
    } else {
      return handleException(e);
    }
  }
}
