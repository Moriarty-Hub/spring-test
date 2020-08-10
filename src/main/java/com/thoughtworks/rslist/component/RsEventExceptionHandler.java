package com.thoughtworks.rslist.component;

import com.thoughtworks.rslist.api.RsController;
import com.thoughtworks.rslist.exception.Error;
import com.thoughtworks.rslist.exception.FailedToBuyRankException;
import com.thoughtworks.rslist.exception.RequestNotValidException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(assignableTypes = RsController.class)
public class RsEventExceptionHandler {

    @ExceptionHandler(FailedToBuyRankException.class)
    public ResponseEntity<Error> failedToBuyRankExceptionHandler(FailedToBuyRankException failedToBuyRankException) {
        return ResponseEntity.badRequest().body(new Error(failedToBuyRankException.getMessage()));
    }

    /*@ExceptionHandler(RequestNotValidException.class)
    public ResponseEntity<Error> requestNotValidExceptionHandler(RequestNotValidException requestNotValidException) {
        return ResponseEntity.badRequest().body(new Error((requestNotValidException.getMessage())));
    }*/
}
