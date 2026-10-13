package ru.rbs.testovoe.exception;

public final class ErrorCode {

    private ErrorCode() {
    }

    // BK — Booking
    public static final String BOOKING_NOT_FOUND = "BK0001";
    public static final String BOOKING_CONFLICT = "BK0002";
    public static final String BOOKING_INVALID_TIME = "BK0003";

    // VA — Validation
    public static final String VALIDATION_FAILED = "VA0001";
    public static final String VALIDATION_BAD_REQUEST = "VA0002";

    // SY — System
    public static final String INTERNAL_ERROR = "SY0001";
    public static final String REQUEST_NOT_SUPPORTED = "SY0002";
}
