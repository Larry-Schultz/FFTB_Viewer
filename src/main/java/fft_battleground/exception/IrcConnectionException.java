package fft_battleground.exception;

public class IrcConnectionException extends BattleGroundException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3749812557547412694L;

	public IrcConnectionException(Exception e) {
		super(e);
	}
	
	public IrcConnectionException(String msg) {
		super(msg);
	}
	
	public IrcConnectionException(String msg, Exception e) {
		super(msg, e);
	}

}
