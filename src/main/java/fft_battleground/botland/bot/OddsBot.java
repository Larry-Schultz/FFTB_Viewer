package fft_battleground.botland.bot;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import fft_battleground.botland.model.Bet;
import fft_battleground.botland.model.BetType;
import fft_battleground.botland.model.BotParam;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.util.GambleUtil;

public class OddsBot extends BetterBetBot {

	private String NAME = "oddsBot";
	protected BetType type;
	protected String betAmountExpression;
	
	public OddsBot(Integer currentAmountToBetWith, BattleGroundTeam left,
			BattleGroundTeam right) {
		super(currentAmountToBetWith, left, right);
	}

	@Override
	public String getName() {
		return this.NAME;
	}
	
	@Override
	public void initParams(Map<String, BotParam> map) {
		if(map.containsKey(PERSONALITY_PARAM)) {
			this.personalityName = map.get(PERSONALITY_PARAM).getValue();
		}
		if(map.containsKey(INVERSE_PARAM)) {
			this.inverse = Boolean.valueOf(map.get(INVERSE_PARAM).getValue());
		}
	}
	
	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Float generateLeftScore() {
		Integer leftSum = this.getSumOfLeftTeam();
		return leftSum.floatValue();
	}

	@Override
	protected Float generateRightScore() {
		Integer rightSum = this.getSumOfRightTeam();
		return rightSum.floatValue();
	}

	@Override
	protected Bet generateBetAmount(Float leftScore, Float rightScore, BattleGroundTeam chosenTeam) {
		Integer result = null;
		Integer leftSum = leftScore.intValue();
		Integer rightSum = rightScore.intValue();
		if(leftScore > rightScore) {
			if(this.type == BetType.ALLIN) {
				Float betAmountFloat =  GambleUtil.bettingPercentage(leftSum, rightSum) * ((float)GambleUtil.MAX_BET);
				result = Collections.min(Arrays.asList(new Integer[] {this.currentAmountToBetWith, betAmountFloat.intValue(), GambleUtil.MAX_BET}));
			}
		} else {
			if(this.type == BetType.ALLIN) {
				Float betAmountFloat =  GambleUtil.bettingPercentage(rightSum, leftSum) * ((float)GambleUtil.MAX_BET);
				result = Collections.min(Arrays.asList(new Integer[] {this.currentAmountToBetWith, betAmountFloat.intValue(), GambleUtil.MAX_BET}));
			}
		}
		
		Bet bet = new Bet(chosenTeam, result, this.isBotSubscriber);
		
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
	public void setName(String name) {
		this.NAME = name;
		
	}

}
