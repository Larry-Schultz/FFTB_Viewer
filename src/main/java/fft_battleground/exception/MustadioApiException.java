package fft_battleground.exception;

public class MustadioApiException extends BattleGroundException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2667727057395745505L;

	public MustadioApiException(Exception e) {
		super(e);
	}
	
	public MustadioApiException(Exception e, String msg) {
		super(msg, e);
	}
}
