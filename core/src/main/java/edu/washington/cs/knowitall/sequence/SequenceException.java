package edu.washington.cs.knowitall.sequence;

/**
 * An exception class used for handling errors related to layered sequences.
 * 
 * @author afader
 * 
 */
public class SequenceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SequenceException(Exception cause) {
        super(cause);
    }

    public SequenceException(String message, Exception cause) {
        super(message, cause);
    }

    public SequenceException(String message) {
        super(message);
    }
}
