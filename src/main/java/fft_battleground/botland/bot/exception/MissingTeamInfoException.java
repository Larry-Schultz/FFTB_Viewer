package fft_battleground.botland.bot.exception;

public class MissingTeamInfoException extends BotConfigException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4600675962019234868L;

	public MissingTeamInfoException(String message) {
		super(message);
	}
	
	public MissingTeamInfoException(Exception e) {
		super(e);
	}

	public MissingTeamInfoException(String string, NullPointerException e) {
		super(string, e);
	}
}
