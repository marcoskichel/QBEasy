package br.com.mk.qbeasy.model.exception;

public class QBEasyException extends RuntimeException{
	private static final long serialVersionUID = 2049215625574389968L;
	
	public QBEasyException(String message) {
		super(message);
	}
	
	public QBEasyException(Throwable cause) {
		super(cause);
	}
	
	public QBEasyException(String message, Throwable cause) {
		super(message, cause);
	}
}
