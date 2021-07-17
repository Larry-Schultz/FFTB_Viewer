package fft_battleground.botland.bot;

import java.util.Map;

import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Constant;
import org.mariuszgromada.math.mxparser.Expression;

import fft_battleground.botland.model.Bet;
import fft_battleground.botland.model.BotParam;
import fft_battleground.exception.BotConfigException;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.util.GambleUtil;

public class TeamValueBot extends BetterBetBot {

	private String name;
	private String betAmountExpression;
	
	public TeamValueBot(Integer currentAmountToBetWith, BattleGroundTeam left, BattleGroundTeam right) {
		super(currentAmountToBetWith, left, right);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initParams(Map<String, BotParam> map) throws BotConfigException {
		if(map.containsKey(BET_AMOUNT_EXPRESSION_PARAMETER)) {
			this.betAmountExpression = map.get(BET_AMOUNT_EXPRESSION_PARAMETER).getValue();
		}
		if(map.containsKey(PERSONALITY_PARAM)) {
			super.personalityName = map.get(PERSONALITY_PARAM).getValue();
		}
		if(map.containsKey(INVERSE_PARAM)) {
			super.inverse = Boolean.valueOf(map.get(INVERSE_PARAM).getValue());
		}
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	protected Float generateLeftScore() {
		Float result = super.teamData.getLeftTeamData().getTeamValue().floatValue();
		return result;
	}

	@Override
	protected Float generateRightScore() {
		Float result = super.teamData.getRightTeamData().getTeamValue().floatValue();
		return result;
	}

	@Override
	protected Bet generateBetAmount(Float leftScore, Float rightScore, BattleGroundTeam chosenTeam) {
		Integer betAmount = this.calculateBetAmount(leftScore, rightScore);
		result = new Bet(chosenTeam, betAmount, this.isBotSubscriber);
		return result;
	}
	
	protected Integer calculateBetAmount(Float leftScore, Float rightScore) {
		Integer result = null;
		
		Argument leftScoreArg = new Argument("leftScore", (double) leftScore);
		Argument rightScoreArg = new Argument("rightScore", (double) rightScore);
		Constant minBet = new Constant("mnBet", (double) GambleUtil.getMinimumBetForBettor(this.isBotSubscriber));
		Constant maxBet = new Constant("mxBet", (double) GambleUtil.MAX_BET);
		Argument balanceArg = new Argument("balance", this.currentAmountToBetWith);
		
		Expression exp = new Expression(this.betAmountExpression, leftScoreArg, rightScoreArg, minBet, maxBet, balanceArg);
		
		result = new Double(exp.calculate()).intValue();
		
		return result;
	}

}
