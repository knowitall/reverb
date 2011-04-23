package edu.washington.cs.knowitall.index;

public class ExtractionFormatException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ExtractionFormatException(String message) {
		super(message);
	}
	
	public ExtractionFormatException(Exception cause) {
        super(cause);
    }

}
