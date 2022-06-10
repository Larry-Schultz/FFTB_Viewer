package fft_battleground.botland.bot;

import java.util.Map;

import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Constant;
import org.mariuszgromada.math.mxparser.Expression;

import fft_battleground.botland.bot.model.Bet;
import fft_battleground.botland.bot.util.BetterBetBot;
import fft_battleground.botland.bot.util.BotCanInverse;
import fft_battleground.botland.bot.util.BotCanUseBetExpressions;
import fft_battleground.botland.bot.util.BotContainsPersonality;
import fft_battleground.botland.bot.util.BotParameterReader;
import fft_battleground.botland.model.BotParam;
import fft_battleground.exception.BotConfigException;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.util.GambleUtil;

public class TeamValueBot 
extends BetterBetBot
implements BotContainsPersonality, BotCanInverse, BotCanUseBetExpressions{

	private String name;
	private String betAmountExpression;
	
	public TeamValueBot(Integer currentAmountToBetWith, BattleGroundTeam left, BattleGroundTeam right) {
		super(currentAmountToBetWith, left, right);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initParams(Map<String, BotParam> map) throws BotConfigException {
		BotParameterReader reader = new BotParameterReader(map);
		this.betAmountExpression = this.readBetAmountExpression(reader)
				.orElseThrow(BotParameterReader.throwBotconfigException("missing parameter " + this.getBetAmountExpressionParameter() + " for bot " + this.name));
		this.personalityName = this.readPersonalityParam(reader);
		this.inverse = this.readInverseParameter(reader);
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
