package fft_battleground.botland.model;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.model.BattleGroundTeam;
import fft_battleground.util.GambleUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Bet {
	private BattleGroundTeam team;
	private Integer amount;
	private BetType type = BetType.VALUE;
	
	public Bet(BattleGroundTeam team, Integer amount) {
		this.team = team;
		this.amount = amount;
		this.type = BetType.VALUE;
	}
	
	public Bet(BattleGroundTeam team, BetType type) {
		this.team = team;
		this.type = type;
		this.amount = GambleUtil.MINIMUM_BET;
		
	}
	
	public String generateBetString() {
		String result = null;
		switch(this.type) {
		case VALUE:
			result = "!bet " + BattleGroundTeam.getTeamName(team) + " " + String.valueOf(this.amount);
			break;
		case ALLIN:
			result = "!allin " + BattleGroundTeam.getTeamName(team);
			break;
		case FLOOR:
			result = "!betf " + BattleGroundTeam.getTeamName(team);
			break;
		case HALF:
			result = "!bet half " + BattleGroundTeam.getTeamName(team);
			break;
		case PERCENTAGE:
			result = "!bet " + String.valueOf(this.amount) + "% " + BattleGroundTeam.getTeamName(team);
			break;
		default: //for default case just bet floor
			log.warn("uncovered case for Bet String with Bet Type : {}", this.type);
			result = "!betf " + BattleGroundTeam.getTeamName(team);
			break;
		}
		
		return result;
	}
	
	public Integer getBetAmount(Integer balance) {
		Integer value = GambleUtil.MINIMUM_BET;
		switch(this.type) {
		case VALUE:
			value = this.amount;
			break;
		case PERCENTAGE:
			Integer lastKnownAmount = balance;
			if(lastKnownAmount != null) {
				Integer percentage = Integer.valueOf(this.amount);
				value = new Integer( (int) (lastKnownAmount.floatValue() * percentage.floatValue() * 0.01f));
			}
			break;
		case ALLIN:
			if(balance != null) {
				value = balance;
			} else {
				value = GambleUtil.MINIMUM_BET;
			}
			break;
		case HALF:
			if(balance != null) {
				value = balance/2;
			} else {
				value = GambleUtil.MINIMUM_BET;
			}
			//no need to implement allinbut code here, since allinbut half is the same as bet half.
			break;
		case FLOOR:
			value = GambleUtil.MINIMUM_BET;
			break;
		default:
			break;
		}
	
		return value;
	}
	
}
