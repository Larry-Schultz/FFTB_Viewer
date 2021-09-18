package fft_battleground.exception;

public class AscensionException extends BattleGroundException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8532234102749818514L;
	
	private String cleanedName;
	
	public AscensionException(Throwable e, String cleanedName) {
		super(defaultMessage(cleanedName), e);
		this.cleanedName = cleanedName;
	}
	
	public AscensionException(Throwable e, String cleanedName, String message) {
		super(message, e);
		this.cleanedName = cleanedName;
	}
	
	protected static String defaultMessage(String cleanedName) {
		return "Error processing Ascension refresh for player" + cleanedName;
	}
}
