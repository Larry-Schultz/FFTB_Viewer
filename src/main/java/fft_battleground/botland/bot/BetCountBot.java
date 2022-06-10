package fft_battleground.botland.bot;

import java.util.Map;
import java.util.Optional;

import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Constant;
import org.mariuszgromada.math.mxparser.Expression;

import fft_battleground.botland.bot.model.Bet;
import fft_battleground.botland.bot.model.BetType;
import fft_battleground.botland.bot.util.BetterBetBot;
import fft_battleground.botland.bot.util.BotCanInverse;
import fft_battleground.botland.bot.util.BotCanUseBetExpressions;
import fft_battleground.botland.bot.util.BotContainsPersonality;
import fft_battleground.botland.bot.util.BotParameterReader;
import fft_battleground.botland.model.BotParam;
import fft_battleground.exception.BotConfigException;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.util.GambleUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BetCountBot 
extends BetterBetBot 
implements BotContainsPersonality, BotCanInverse, BotCanUseBetExpressions {

	private static final String BET_AMOUNT_PARAMETER = "betAmount";
	private static final String BET_TYPE_PARAMETER = "betType";
	
	public BetCountBot(Integer currentAmountToBetWith, BattleGroundTeam left,
			BattleGroundTeam right) {
		super(currentAmountToBetWith, left, right);
		// TODO Auto-generated constructor stub
	}

	private String name = "minBetBot";
	
	private Optional<Integer> amount;
	private BetType type = BetType.FLOOR;
	private Optional<String> betAmountExpression;
	
	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public void initParams(Map<String, BotParam> parameters) throws BotConfigException {
		BotParameterReader reader = new BotParameterReader(parameters);
		this.amount = reader.readParam(BET_AMOUNT_PARAMETER, Integer::valueOf);
		this.type = reader.readParam(BET_TYPE_PARAMETER, BetType::getBetType)
				.orElseThrow(BotParameterReader.throwBotconfigException("Missing Bet Type"));
		this.betAmountExpression = this.readBetAmountExpression(reader);
		this.personalityName = this.readPersonalityParam(reader);
		this.inverse = this.readInverseParameter(reader);
		
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
	protected Bet generateBetAmount(Float leftScore, Float rightScore, BattleGroundTeam chosenTeam) throws BotConfigException {
		Bet result = null;
		if(type == BetType.FLOOR) {
			result = new Bet(chosenTeam, BetType.FLOOR, this.isBotSubscriber);
		} else if(type == BetType.ALLIN) {
			result = new Bet(chosenTeam, BetType.ALLIN, this.isBotSubscriber);
		} else if(type == BetType.PERCENTAGE) {
			Integer betAmount = this.amount.orElseThrow(BotParameterReader.throwBotconfigException("Missing Bet Aount"));
			result = new Bet(chosenTeam, betAmount, this.type, this.isBotSubscriber);
		} else if(this.betAmountExpression.isPresent()) {
			Integer betAmount = this.calculateBetAmount(this.betAmountExpression.get(), leftScore, rightScore);
			result = new Bet(chosenTeam, betAmount, this.isBotSubscriber);
		}
		return result;
	}
	
	protected Integer calculateBetAmount(String betAmountExpression, Float leftScore, Float rightScore) {
		Integer result = null;
		
		Argument leftScoreArg = new Argument("leftScore", (double) leftScore);
		Argument rightScoreArg = new Argument("rightScore", (double) rightScore);
		Constant minBet = new Constant("mnBet", (double) GambleUtil.getMinimumBetForBettor(this.isBotSubscriber));
		Constant maxBet = new Constant("mxBet", (double) GambleUtil.MAX_BET);
		Argument balanceArg = new Argument("balance", this.currentAmountToBetWith);
		
		Expression exp = new Expression(betAmountExpression, leftScoreArg, rightScoreArg, minBet, maxBet, balanceArg);
		
		result = Double.valueOf(exp.calculate()).intValue();
		
		return result;
	}

	@Override
	public void init() {
		if(this.betAmountExpression.isPresent()) {
			Argument leftScoreArg = new Argument("leftScore", (double) 5f);
			Argument rightScoreArg = new Argument("rightScore", (double) 10f);
			Argument minBet = new Argument("mnBet", (double) GambleUtil.getMinimumBetForBettor(this.isBotSubscriber));
			Argument maxBet = new Argument("mxBet", (double) GambleUtil.MAX_BET);
			Argument balanceArg = new Argument("balance", this.currentAmountToBetWith);
			Expression testBetAmountExpression = new Expression(this.betAmountExpression.get(), leftScoreArg, rightScoreArg, minBet, maxBet, balanceArg);
			if(!testBetAmountExpression.checkSyntax() || !testBetAmountExpression.checkLexSyntax()) {
				log.warn("The syntax of the bet Amount expression {} is faulty with error: {}", this.betAmountExpression, testBetAmountExpression.getErrorMessage());
			}
		}
		
	}

}
