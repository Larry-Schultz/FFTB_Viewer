package fft_battleground.exception;

public class DumpException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8635987846622158116L;
	
	public DumpException() {}
	
	public DumpException(Exception e) {
		super(e);
	}
	
	public DumpException(Exception e, String msg) {
		super(msg, e);
	}

}
