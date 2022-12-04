package fft_battleground.botland.bot.exception;

public class MissingPlayerMetadataException extends BotConfigException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1247865888184329847L;

	public MissingPlayerMetadataException(String message) {
		super(message);
	}
	
	public MissingPlayerMetadataException(Exception e) {
		super(e);
	}

	public MissingPlayerMetadataException(String string, NullPointerException e) {
		super(string, e);
	}
}
