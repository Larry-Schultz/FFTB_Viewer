package fft_battleground.exception;

public class BattleGroundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2537683734930377894L;

	public BattleGroundException(Exception e) {
		super(e);
	}

	public BattleGroundException(String msg, Throwable e) {
		super(msg, e);
	}

	public BattleGroundException(String string) {
		super(string);
	}

	public BattleGroundException() {
		super();
	}

}
