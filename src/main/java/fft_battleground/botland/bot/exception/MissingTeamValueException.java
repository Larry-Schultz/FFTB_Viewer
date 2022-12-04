package fft_battleground.botland.bot.exception;

public class MissingTeamValueException extends BotConfigException {

	private static final long serialVersionUID = -5570712457901964672L;

	public MissingTeamValueException(String message) {
		super(message);
	}
	
	public MissingTeamValueException(Exception e) {
		super(e);
	}

	public MissingTeamValueException(String string, NullPointerException e) {
		super(string, e);
	}

}
