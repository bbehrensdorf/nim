package net.behrensdorf.nim;

public class NimException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NimException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

	public NimException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public NimException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	NimException() {
		super();
	}

	NimException(String message) {
		super(message);
	}

}
