package fft_battleground.botland.personality;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import fft_battleground.botland.personality.model.PersonalityDisplayFields;
import fft_battleground.botland.personality.model.PersonalityMatrixEntry;
import fft_battleground.model.BattleGroundTeam;

public class NerdPersonality extends PersonalityModule {

	private static Map<Integer, PersonalityMatrixEntry> personalityDataMatrix;
	
	static {
		PersonalityMatrixEntry[] entries = new PersonalityMatrixEntry[] {
				new PersonalityMatrixEntry(0, 2, "Nope, not getting into this one.  I can't tell them apart as their scores are identical", true),
				new PersonalityMatrixEntry(3, 10,
						map -> new StringBuilder("Wow both ").append(map.get(PersonalityDisplayFields.LEFT_TEAM_PROPER)).append(" and ").append(map.get(PersonalityDisplayFields.RIGHT_TEAM_PROPER))
						.append(" are super close, with a difference of ").append(map.get(PersonalityDisplayFields.DIFFERENCE)).append("(").append(map.get(PersonalityDisplayFields.PERCENTILE))
						.append("%). Betting the minimum.")
						.toString(),
						false,
						new PersonalityDisplayFields[] {PersonalityDisplayFields.LEFT_TEAM_PROPER, PersonalityDisplayFields.RIGHT_TEAM_PROPER, PersonalityDisplayFields.DIFFERENCE, PersonalityDisplayFields.PERCENTILE}
				),
				new PersonalityMatrixEntry(10, 25, 
						map -> new StringBuilder("These teams are pretty closesly matched. The ").append(map.get(PersonalityDisplayFields.WINNING_TEAM_PROPER))
							.append(" team has a score of ").append(map.get(PersonalityDisplayFields.WINNING_SCORE)).append(" and the ")
							.append(map.get(PersonalityDisplayFields.LOSING_TEAM_PROPER)).append(" has a score of ").append(map.get(PersonalityDisplayFields.LOSING_SCORE))
							.append(". A ").append(map.get(PersonalityDisplayFields.PERCENTILE)).append("% difference.")
							.toString(), 
						false, 
						new PersonalityDisplayFields[] {PersonalityDisplayFields.WINNING_TEAM_PROPER, PersonalityDisplayFields.WINNING_SCORE, PersonalityDisplayFields.LOSING_TEAM_PROPER, 
								PersonalityDisplayFields.LOSING_SCORE, PersonalityDisplayFields.PERCENTILE
							}
				),
				new PersonalityMatrixEntry(26, 75, 
					map -> new StringBuilder("Betting on ").append(map.get(PersonalityDisplayFields.WINNING_TEAM_PROPER)).append(" as it has the higher score ")
						.append(map.get(PersonalityDisplayFields.WINNING_SCORE)).append(" vs ").append(map.get(PersonalityDisplayFields.LOSING_SCORE)).append(". A ")
						.append(map.get(PersonalityDisplayFields.PERCENTILE)).append("% difference.")
						.toString(),
					false, 
					new PersonalityDisplayFields[] {PersonalityDisplayFields.WINNING_TEAM_PROPER, PersonalityDisplayFields.WINNING_SCORE, PersonalityDisplayFields.LOSING_SCORE, PersonalityDisplayFields.PERCENTILE}
				),
				new PersonalityMatrixEntry(76, 98,
					map -> new StringBuilder("The ").append(map.get(PersonalityDisplayFields.WINNING_TEAM_PROPER)).append(" team is beating the ").append(map.get(PersonalityDisplayFields.LOSING_TEAM_PROPER))
						.append(" team by over ").append(map.get(PersonalityDisplayFields.DIFFERENCE)).append(" points! That is in the top ").append(map.get(PersonalityDisplayFields.PERCENTILE))
						.append("% of score differences").toString(),
					false,
					new PersonalityDisplayFields[] {PersonalityDisplayFields.WINNING_TEAM_PROPER, PersonalityDisplayFields.LOSING_TEAM_PROPER, PersonalityDisplayFields.DIFFERENCE, PersonalityDisplayFields.PERCENTILE}
				),
				new PersonalityMatrixEntry(98, 100,
					map -> new StringBuilder("You should all in.  My data suggests the ").append(map.get(PersonalityDisplayFields.WINNING_TEAM_PROPER)).append(" has over a ")
					.append(map.get(PersonalityDisplayFields.PERCENTILE)).append("% chance to win!")
						.toString(),
					true,
					new PersonalityDisplayFields[] {PersonalityDisplayFields.WINNING_TEAM_PROPER, PersonalityDisplayFields.PERCENTILE}
				)
				
				
		};
		personalityDataMatrix = NerdPersonality.buildPersonalityMatrix(entries);
	}
	
	@Override
	public String personalityString(String botName, Float leftScore, BattleGroundTeam leftTeam, Float rightScore,
			BattleGroundTeam rightTeam, Map<Integer, Integer> percentiles, Integer percentile) {
		PersonalityMatrixEntry entry = NerdPersonality.personalityDataMatrix.get(percentile);
		List<PersonalityDisplayFields> requiredFields = entry.getRequiredFields();
		
		Map<PersonalityDisplayFields, String> fieldData = PersonalityDisplayFields.generateFieldData(leftScore, leftTeam, rightScore, rightTeam, percentiles, percentile, requiredFields);
		String personality = entry.getMessage(fieldData);
		
		return personality;
	}
	
	@Override
	protected boolean displayResponse(String botName, Float leftScore, BattleGroundTeam leftTeam, Float rightScore,
			BattleGroundTeam rightTeam, Map<Integer, Integer> percentiles, Integer percentile) {
		PersonalityMatrixEntry entry = NerdPersonality.personalityDataMatrix.get(percentile);
		boolean display = entry.isDisplay();
		return display;
	}
	
	private static Map<Integer, PersonalityMatrixEntry> buildPersonalityMatrix(PersonalityMatrixEntry[] entries) {
		Map<Integer, PersonalityMatrixEntry> matrix = new HashMap<>();
		for(PersonalityMatrixEntry entry: entries) {
			int[] keys = IntStream.range(entry.getBeginInclusive(), entry.getEndInclusive() + 1).toArray();
			for(Integer key: keys) {
				matrix.put(key, entry);
			}
		}
		
		return matrix;
	}

}
