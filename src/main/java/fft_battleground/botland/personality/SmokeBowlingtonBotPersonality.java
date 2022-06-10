package fft_battleground.botland.personality;

import java.util.Map;

import fft_battleground.botland.personality.model.PersonalityDisplayFields;
import fft_battleground.botland.personality.model.PersonalityMatrixEntry;
import fft_battleground.model.BattleGroundTeam;

public class SmokeBowlingtonBotPersonality extends PersonalityModule {

	protected static PersonalityMatrixEntry smokebowlingtonbotPersonality = new PersonalityMatrixEntry(map -> new StringBuilder("[Kozuka] my bet is !bet ").append(map.get(PersonalityDisplayFields.WINNING_TEAM_INFORMAL))
			.append(" ").append(map.get(PersonalityDisplayFields.WINNING_SCORE)).toString(),
		false,
		new PersonalityDisplayFields[] {PersonalityDisplayFields.WINNING_TEAM_INFORMAL, PersonalityDisplayFields.WINNING_SCORE,});
	
	private PersonalityMatrixEntry entry;
	
	public SmokeBowlingtonBotPersonality() {
		this.entry = smokebowlingtonbotPersonality;
	}
	
	@Override
	protected String personalityString(String botName, Float leftScore, BattleGroundTeam leftTeam, Float rightScore,
			BattleGroundTeam rightTeam, Map<Integer, Integer> percentiles, Integer percentile) {
		Map<PersonalityDisplayFields, String> map = PersonalityDisplayFields.generateFieldData(leftScore, leftTeam, rightScore, rightTeam, percentiles, percentile, this.entry.getRequiredFields());
		String message = this.entry.getMessage(map);
		return message;
	}

}
