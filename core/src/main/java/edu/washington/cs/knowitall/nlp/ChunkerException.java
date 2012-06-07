package edu.washington.cs.knowitall.nlp;

/**
 * An exception class used for handling errors related to layered sequences.
 *
 * @author afader
 *
 */
public class ChunkerException extends NlpException {

    private static final long serialVersionUID = 1L;

    public ChunkerException(Exception cause) {
        super(cause);
    }

    public ChunkerException(String message, Exception cause) {
        super(message, cause);
    }

    public ChunkerException(String message) {
        super(message);
    }
}
