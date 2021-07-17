package fft_battleground.botland.personality;

import java.util.Map;

import fft_battleground.botland.personality.model.BraveFaith;
import fft_battleground.botland.personality.model.PersonalityDisplayFields;
import fft_battleground.botland.personality.model.PersonalityMatrixEntry;
import fft_battleground.model.BattleGroundTeam;

public class BraveFaithPersonalityModule extends PersonalityModule {

	private BraveFaith braveFaith = BraveFaith.NONE;
	private boolean inverse = false;
	private PersonalityMatrixEntry relevantEntry = braveEntry;
	
	protected static final PersonalityMatrixEntry braveEntry = new PersonalityMatrixEntry(map -> new StringBuilder("The Brave shall prevail! The ")
			.append(map.get(PersonalityDisplayFields.WINNING_TEAM_PROPER)).append(" ").append(map.get(PersonalityDisplayFields.WINNING_TEAM_INFORMAL)).append(" are more brave than the")
			.append(map.get(PersonalityDisplayFields.LOSING_TEAM_PROPER)).append(" ").append(map.get(PersonalityDisplayFields.LOSING_TEAM_INFORMAL))
			.append(", with bravery of ").append(map.get(PersonalityDisplayFields.WINNING_SCORE)).append(" vs ").append(map.get(PersonalityDisplayFields.LOSING_SCORE)).append(".")
			.toString(),
		false,
		new PersonalityDisplayFields[] {PersonalityDisplayFields.LOSING_TEAM_PROPER, PersonalityDisplayFields.LOSING_TEAM_INFORMAL, PersonalityDisplayFields.WINNING_TEAM_PROPER, 
				PersonalityDisplayFields.WINNING_TEAM_INFORMAL, PersonalityDisplayFields.LOSING_SCORE, PersonalityDisplayFields.WINNING_SCORE,});
	
	protected static final PersonalityMatrixEntry faithEntry = new PersonalityMatrixEntry(map -> new StringBuilder("The Faithful shall perservere! ")
			.append(map.get(PersonalityDisplayFields.WINNING_TEAM_PROPER)).append(" has more faith than ").append(map.get(PersonalityDisplayFields.LOSING_TEAM_PROPER))
			.append(", with faith of ").append(map.get(PersonalityDisplayFields.WINNING_SCORE)).append(" vs ").append(map.get(PersonalityDisplayFields.LOSING_SCORE)).append(".")
			.toString(),
		false,
		new PersonalityDisplayFields[] {PersonalityDisplayFields.LOSING_TEAM_PROPER, PersonalityDisplayFields.WINNING_TEAM_PROPER, PersonalityDisplayFields.LOSING_SCORE,
				PersonalityDisplayFields.WINNING_SCORE,});
	
	protected static final PersonalityMatrixEntry cowardEntry = new PersonalityMatrixEntry(map -> new StringBuilder("The meek shall inherit the Earth! The ")
			.append(map.get(PersonalityDisplayFields.WINNING_TEAM_PROPER)).append(" ").append(map.get(PersonalityDisplayFields.LOSING_TEAM_INFORMAL)).append(" is more cowardly and shrewd than the ")
			.append(map.get(PersonalityDisplayFields.LOSING_TEAM_PROPER)).append(" ").append(map.get(PersonalityDisplayFields.WINNING_TEAM_INFORMAL))
			.append(", with bravery of ").append(map.get(PersonalityDisplayFields.LOSING_SCORE)).append(" vs ").append(map.get(PersonalityDisplayFields.WINNING_SCORE)).append(".")
			.toString(),
		false,
		new PersonalityDisplayFields[] {PersonalityDisplayFields.LOSING_TEAM_PROPER, PersonalityDisplayFields.LOSING_TEAM_INFORMAL, PersonalityDisplayFields.WINNING_TEAM_PROPER, 
				PersonalityDisplayFields.WINNING_TEAM_INFORMAL, PersonalityDisplayFields.LOSING_SCORE, PersonalityDisplayFields.WINNING_SCORE,});
	
	protected static final PersonalityMatrixEntry athiestEntry = new PersonalityMatrixEntry(map -> new StringBuilder("Faith is a sign of weakness, science is the way of the future! ")
			.append(map.get(PersonalityDisplayFields.LOSING_TEAM_PROPER)).append(" is less faithful than ").append(map.get(PersonalityDisplayFields.WINNING_TEAM_PROPER))
			.append(", with faith of ").append(map.get(PersonalityDisplayFields.LOSING_SCORE)).append(" vs ").append(map.get(PersonalityDisplayFields.WINNING_SCORE)).append(".")
			.toString(),
		false,
		new PersonalityDisplayFields[] {PersonalityDisplayFields.LOSING_TEAM_PROPER, PersonalityDisplayFields.WINNING_TEAM_PROPER, PersonalityDisplayFields.LOSING_SCORE,
				PersonalityDisplayFields.WINNING_SCORE,});
	
	protected static final PersonalityMatrixEntry numbersEntry = new PersonalityMatrixEntry(map -> new StringBuilder("Big numbers are always better! ")
			.append(map.get(PersonalityDisplayFields.WINNING_TEAM_PROPER)).append(" has more bravery and faith than ").append(map.get(PersonalityDisplayFields.LOSING_TEAM_PROPER))
			.append(", with totals of ").append(map.get(PersonalityDisplayFields.WINNING_SCORE)).append(" vs ").append(map.get(PersonalityDisplayFields.LOSING_SCORE)).append(".")
			.toString(),
		false,
		new PersonalityDisplayFields[] {PersonalityDisplayFields.LOSING_TEAM_PROPER, PersonalityDisplayFields.WINNING_TEAM_PROPER, PersonalityDisplayFields.LOSING_SCORE,
				PersonalityDisplayFields.WINNING_SCORE,});
	
	protected static final PersonalityMatrixEntry noNumbersEntry = new PersonalityMatrixEntry(map -> new StringBuilder("Math is hard, I hate numbers! The")
			.append(map.get(PersonalityDisplayFields.LOSING_TEAM_PROPER)).append(" ").append(map.get(PersonalityDisplayFields.LOSING_TEAM_INFORMAL)).append(" have fewer brave and faith number thingies than ")
			.append(map.get(PersonalityDisplayFields.WINNING_TEAM_PROPER)).append(" ").append(map.get(PersonalityDisplayFields.WINNING_TEAM_INFORMAL))
			.append(", with maths of ").append(map.get(PersonalityDisplayFields.LOSING_SCORE)).append(" vs ").append(map.get(PersonalityDisplayFields.WINNING_SCORE)).append(".")
			.toString(),
		false,
		new PersonalityDisplayFields[] {PersonalityDisplayFields.LOSING_TEAM_PROPER, PersonalityDisplayFields.LOSING_TEAM_INFORMAL, PersonalityDisplayFields.WINNING_TEAM_PROPER, 
				PersonalityDisplayFields.WINNING_TEAM_INFORMAL, PersonalityDisplayFields.LOSING_SCORE, PersonalityDisplayFields.WINNING_SCORE,});
	
	protected static final PersonalityMatrixEntry oops = new PersonalityMatrixEntry("oops, missed something creating this personality", false);
	
	public BraveFaithPersonalityModule() {}
	
	public BraveFaithPersonalityModule(BraveFaith braveFaith, boolean inverse) {
		this.braveFaith = braveFaith;
		this.inverse = inverse;
		this.relevantEntry = this.determineProperMatrixEntry(braveFaith, inverse);
	}
	
	
	@Override
	protected String personalityString(String botName, Float leftScore, BattleGroundTeam leftTeam, Float rightScore,
			BattleGroundTeam rightTeam, Map<Integer, Integer> percentiles, Integer percentile) {
		Map<PersonalityDisplayFields, String> displayFieldMap = PersonalityDisplayFields.generateFieldData(leftScore, leftTeam, rightScore, rightTeam, percentiles, percentile, this.relevantEntry.getRequiredFields());
		String response = this.relevantEntry.getMessage(displayFieldMap);
		return response;
	}
	
	protected PersonalityMatrixEntry determineProperMatrixEntry(BraveFaith braveFaith, boolean inverse) {
		PersonalityMatrixEntry relevantEntry = null;
		switch(braveFaith) {
		case BRAVE:
			relevantEntry = !inverse ? braveEntry: cowardEntry;
			break;
		case FAITH:
			relevantEntry = !inverse ? faithEntry: athiestEntry;
			break;
		case BOTH:
			relevantEntry = !inverse ? numbersEntry: noNumbersEntry;
			break;
		default:
			relevantEntry = oops;
			break;
		}
		
		return relevantEntry;
	}

}
