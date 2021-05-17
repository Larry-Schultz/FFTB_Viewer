package fft_battleground.exception;

public class IncorrectTypeException extends DumpException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5822635615373510036L;
	
	public IncorrectTypeException(String msg) {
		super(msg);
	}
	
	public IncorrectTypeException(Exception e) {
		super(e);
	}

	public IncorrectTypeException(Exception e, String msg) {
		super(e, msg);
		// TODO Auto-generated constructor stub
	}

}
