package edu.cit.erag.souvenirpos.shared.exception;

/** Raised when a login key exceeds the allowed number of failed attempts in the window. */
public class TooManyLoginAttemptsException extends RuntimeException {
    public TooManyLoginAttemptsException(String message) {
        super(message);
    }
}
