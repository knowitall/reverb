package edu.washington.cs.knowitall.extractor.conf;

import edu.washington.cs.knowitall.nlp.NlpException;

public class ConfidenceFunctionException extends NlpException {
    private static final long serialVersionUID = 1L;

    public ConfidenceFunctionException(Exception cause) {
        super(cause);
    }

    public ConfidenceFunctionException(String message, Exception cause) {
        super(message, cause);
    }
}
