package fft_battleground.exception;

public class AscensionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8532234102749818514L;

	public AscensionException() {}
	
	public AscensionException(Exception e) {
		super(e);
	}
	
	public AscensionException(Throwable e, String msg) {
		super(msg, e);
	}

}
