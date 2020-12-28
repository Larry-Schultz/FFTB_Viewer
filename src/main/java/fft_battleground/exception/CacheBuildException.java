package fft_battleground.exception;

public class CacheBuildException extends BattleGroundException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9189050717646538134L;
	
	public CacheBuildException(Exception e) {
		super(e);
	}
	
	public CacheBuildException(String msg, Exception e) {
		super(msg, e);
	}

}
