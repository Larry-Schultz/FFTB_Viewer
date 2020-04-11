package fft_battleground.bot;

import java.util.List;

import fft_battleground.bot.model.event.BetEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.util.GambleUtil;

public class OddsBot extends BetBot {

	private static final String NAME = "oddsBot";
	
	public OddsBot(Integer currentAmountToBetWith, List<BetEvent> otherPlayerBets, BattleGroundTeam team1,
			BattleGroundTeam team2) {
		super(currentAmountToBetWith, otherPlayerBets, team1, team2);
	}

	@Override
	public String getName() {
		return OddsBot.NAME;
	}

	@Override
	protected Float generateLeftScore() {
		Float score = (float) this.betsBySide.getLeft().size();
		return score;
	}

	@Override
	protected Float generateRightScore() {
		Float score = (float) this.betsBySide.getRight().size();
		return score;
	}

	@Override
	protected Integer generateBetAmount(Float leftScore, Float rightScore, BattleGroundTeam chosenTeam) {
		Integer result = null;
		Integer leftSum = this.getSumOfLeftTeam();
		Integer rightSum = this.getSumOfRightTeam();
		if(leftScore > rightScore) {
			Float betAmountFloat =  GambleUtil.bettingPercentage(leftSum, rightSum) * ((float)GambleUtil.MAX_BET);
			Integer betAmountInt = betAmountFloat.intValue();
			Integer currentAmountToBetWith = this.currentAmountToBetWith;
			result = Math.min(currentAmountToBetWith, betAmountInt);
			if(result > GambleUtil.MAX_BET) {
				result = GambleUtil.MAX_BET; //if we somehow go over, just use the maximum
			}
		} else {
			Float betAmountFloat =  GambleUtil.bettingPercentage(rightSum, leftSum) * ((float)GambleUtil.MAX_BET);
			Integer betAmountInt = betAmountFloat.intValue();
			Integer currentAmountToBetWith = this.currentAmountToBetWith;
			result = Math.min(currentAmountToBetWith, betAmountInt);
			if(result > GambleUtil.MAX_BET) {
				result = GambleUtil.MAX_BET; //if we somehow go over, just use the maximum
			}
		}
		return result;
	}
	
	
	protected Integer getSumOfLeftTeam() {
		Integer result = this.betsBySide.getLeft().stream().mapToInt(betEvent -> betEvent.getBetAmountInteger()).sum();
		return result;
	}
	
	protected Integer getSumOfRightTeam() {
		Integer result = this.betsBySide.getRight().stream().mapToInt(betEvent -> betEvent.getBetAmountInteger()).sum();
		return result;
	}

}
