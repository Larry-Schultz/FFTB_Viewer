package fft_battleground.exception;

public class TournamentApiException extends BattleGroundException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8186248436960145722L;

	public TournamentApiException(Exception e) {
		super(e);
	}
	
	public TournamentApiException(Exception e, String msg) {
		super(msg, e);
	}
}
