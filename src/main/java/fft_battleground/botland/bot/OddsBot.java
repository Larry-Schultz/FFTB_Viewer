package fft_battleground.botland.bot;

import java.util.Map;

import fft_battleground.botland.BetterBetBot;
import fft_battleground.botland.model.Bet;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.util.GambleUtil;

public class OddsBot extends BetterBetBot {

	private String NAME = "oddsBot";
	
	public OddsBot(Integer currentAmountToBetWith, BattleGroundTeam left,
			BattleGroundTeam right) {
		super(currentAmountToBetWith, left, right);
	}

	@Override
	public String getName() {
		return this.NAME;
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
	protected Bet generateBetAmount(Float leftScore, Float rightScore, BattleGroundTeam chosenTeam) {
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
		
		Bet bet = new Bet(chosenTeam, result);
		
		return bet;
	}
	
	
	protected Integer getSumOfLeftTeam() {
		Integer result = this.betsBySide.getLeft().stream().mapToInt(betEvent -> betEvent.getBetAmountInteger()).sum();
		return result;
	}
	
	protected Integer getSumOfRightTeam() {
		Integer result = this.betsBySide.getRight().stream().mapToInt(betEvent -> betEvent.getBetAmountInteger()).sum();
		return result;
	}

	@Override
	public void initParams(Map<String, String> map) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setName(String name) {
		this.NAME = name;
		
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

}
