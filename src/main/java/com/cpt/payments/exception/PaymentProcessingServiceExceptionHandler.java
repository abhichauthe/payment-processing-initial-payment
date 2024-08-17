package com.cpt.payments.exception;

import org.springframework.core.log.LogMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.cpt.payments.constants.ErrorCodeEnum;
import com.cpt.payments.pojo.ErrorResponse;

@ControllerAdvice
public class PaymentProcessingServiceExceptionHandler {

	@ExceptionHandler(PaymentProcessingException.class)
	public ResponseEntity<ErrorResponse> handleProcessingException(PaymentProcessingException ex) {
		System.out.println(" processing exception is -> " + ex.getErrorMessage());
		
		ErrorResponse errorResponse = ErrorResponse.builder()
				.errorCode(ex.getErrorCode())
				.errorMessage(ex.getErrorMessage())
				.build();
		
		System.out.println(" paymentResponse is -> " + errorResponse);
		return new ResponseEntity<>(errorResponse, ex.getHttpStatus());
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
		System.out.println(" generic exception message is -> " + ex.getMessage());
		
		ErrorResponse errorResponse = ErrorResponse.builder()
				.errorCode(ErrorCodeEnum.GENERIC_EXCEPTION.getErrorCode())
				.errorMessage(ErrorCodeEnum.GENERIC_EXCEPTION.getErrorMessage()).build();
		
		System.out.println(" paymentResponse is -> " + errorResponse);
		return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
