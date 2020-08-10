package com.thoughtworks.rslist.exception;

public class FailedToBuyRankException extends RuntimeException {

    private String errorMessage;

    public FailedToBuyRankException(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String getMessage() {
        return errorMessage;
    }
}
