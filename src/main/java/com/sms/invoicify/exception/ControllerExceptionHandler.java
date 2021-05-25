//package com.sms.invoicify.exception;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//
//@ControllerAdvice
//public class ControllerExceptionHandler {
//
//  @ExceptionHandler(value = {InvoicifyCompanyNotExistsException.class})
//
//  protected ResponseEntity<Object> handleConflict(Exception ex) {
//    return new ResponseEntity<>("{\"message\": \"" + ex.getMessage() + "\"}", HttpStatus.BAD_REQUEST);
//  }
//}
