package fft_battleground.exception;

public class DumpException extends BattleGroundException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8635987846622158116L;
	
	public DumpException(String msg) {
		super(msg);
	}
	
	public DumpException(Exception e) {
		super(e);
	}
	
	public DumpException(Exception e, String msg) {
		super(msg, e);
	}

}
