package com.example.telemedicine.exceptions;

/**
 * Exception thrown with PDF generation errors.
 * This exception encapsulates a descriptive message and the root cause of the error.
 * Mainly used in {@link com.example.telemedicine.service.PdfGenerator}.
 */
public class PdfGeneratorException extends Exception {
    public PdfGeneratorException(String message, Throwable cause) {
        super(message, cause);
    }
}
