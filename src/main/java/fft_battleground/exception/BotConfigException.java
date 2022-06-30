package fft_battleground.exception;

public class BotConfigException extends BattleGroundException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5570712457901964672L;

	public BotConfigException(String message) {
		super(message);
	}
	
	public BotConfigException(Exception e) {
		super(e);
	}

	public BotConfigException(String string, NullPointerException e) {
		super(string, e);
	}
}
