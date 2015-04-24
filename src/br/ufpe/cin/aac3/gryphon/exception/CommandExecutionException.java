package br.ufpe.cin.aac3.gryphon.exception;

public class CommandExecutionException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CommandExecutionException(String message, Exception cause) {
		super(message, cause);
	}

	public CommandExecutionException(String message) {
		super(message);
	}
}
