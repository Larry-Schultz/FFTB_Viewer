package fft_battleground.exception;

public class NotANumberBetException extends BattleGroundException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5877396917291055656L;

	public NotANumberBetException(Exception e) {
		super(e);
	}
	
	public NotANumberBetException(Exception e, String msg) {
		super(msg, e);
	}
}
