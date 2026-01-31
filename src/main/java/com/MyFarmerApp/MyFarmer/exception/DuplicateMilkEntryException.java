package com.MyFarmerApp.MyFarmer.exception;

public class DuplicateMilkEntryException extends RuntimeException {
    public DuplicateMilkEntryException(String message) {
        super(message);
    }
}
