package org.grits.toolbox.utils.image;

public class ImageCreationException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 876934541575538012L;

	public ImageCreationException() {

	}

	public ImageCreationException(String message) {
		super(message);
	}

	public ImageCreationException(Throwable cause) {
		super(cause);
	}

	public ImageCreationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ImageCreationException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
