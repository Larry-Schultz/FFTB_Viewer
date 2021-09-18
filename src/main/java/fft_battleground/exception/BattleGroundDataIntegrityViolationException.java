package fft_battleground.exception;

import org.springframework.dao.DataIntegrityViolationException;

public class BattleGroundDataIntegrityViolationException extends BattleGroundException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -593117158271811085L;
	
	private String cleanedName;
	
	public BattleGroundDataIntegrityViolationException() {
		super();
	}
	
	public BattleGroundDataIntegrityViolationException(String cleanedName, DataIntegrityViolationException dive) {
		super(generateErrorMessageWithCleanedName(cleanedName), dive);
		this.setCleanedName(cleanedName);
	}
	
	public BattleGroundDataIntegrityViolationException(String cleanedName) {
		super(generateErrorMessageWithCleanedName(cleanedName));
		this.setCleanedName(cleanedName);
	}
	
	protected static String generateErrorMessageWithCleanedName(String cleanedName) {
		return "data integrity exception found for player " + cleanedName;
	}

	public String getCleanedName() {
		return cleanedName;
	}

	public void setCleanedName(String cleanedName) {
		this.cleanedName = cleanedName;
	}

}
