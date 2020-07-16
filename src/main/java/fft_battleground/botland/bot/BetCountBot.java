package fft_battleground.botland.bot;

import java.util.Map;

import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Constant;
import org.mariuszgromada.math.mxparser.Expression;

import fft_battleground.botland.BetterBetBot;
import fft_battleground.botland.model.Bet;
import fft_battleground.botland.model.BetType;
import fft_battleground.botland.model.BotParam;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.util.GambleUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BetCountBot extends BetterBetBot {

	private static final String BET_AMOUNT_PARAMETER = "betAmount";
	private static final String BET_TYPE_PARAMETER = "betType";
	private static final String BET_AMOUNT_EXPRESSION_PARAMETER = "betExpression";
	
	public BetCountBot(Integer currentAmountToBetWith, BattleGroundTeam left,
			BattleGroundTeam right) {
		super(currentAmountToBetWith, left, right);
		// TODO Auto-generated constructor stub
	}

	private String name = "minBetBot";
	
	private Integer amount;
	private BetType type = BetType.FLOOR;
	private String betAmountExpression;
	
	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public void initParams(Map<String, BotParam> parameters) {
		if(parameters.containsKey(BET_AMOUNT_PARAMETER)) {
			this.amount = Integer.valueOf(parameters.get(BET_AMOUNT_PARAMETER).getValue());
		}
		if(parameters.containsKey(BET_TYPE_PARAMETER)) {
			this.type = BetType.getBetType(parameters.get(BET_TYPE_PARAMETER).getValue());
		}
		if(parameters.containsKey(BET_AMOUNT_EXPRESSION_PARAMETER)) {
			this.betAmountExpression = parameters.get(BET_AMOUNT_EXPRESSION_PARAMETER).getValue();
		}
		
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
		Bet result = null;
		if(type == BetType.FLOOR) {
			result = new Bet(chosenTeam, BetType.FLOOR);
		} else if(type == BetType.ALLIN) {
			result = new Bet(chosenTeam, BetType.ALLIN);
		} else if(type == BetType.PERCENTAGE) {
			result = new Bet(chosenTeam, this.amount, this.type);
		} else if(this.betAmountExpression != null) {
			Integer betAmount = this.calculateBetAmount(leftScore, rightScore);
			result = new Bet(chosenTeam, betAmount);
		}
		return result;
	}
	
	protected Integer calculateBetAmount(Float leftScore, Float rightScore) {
		Integer result = null;
		
		Argument leftScoreArg = new Argument("leftScore", (double) leftScore);
		Argument rightScoreArg = new Argument("rightScore", (double) rightScore);
		Constant minBet = new Constant("mnBet", (double) GambleUtil.MINIMUM_BET);
		Constant maxBet = new Constant("mxBet", (double) GambleUtil.MAX_BET);
		Argument balanceArg = new Argument("balance", this.currentAmountToBetWith);
		
		Expression exp = new Expression(this.betAmountExpression, leftScoreArg, rightScoreArg, minBet, maxBet, balanceArg);
		
		result = new Double(exp.calculate()).intValue();
		
		return result;
	}

	@Override
	public void init() {
		if(this.betAmountExpression != null) {
			Argument leftScoreArg = new Argument("leftScore", (double) 5f);
			Argument rightScoreArg = new Argument("rightScore", (double) 10f);
			Argument minBet = new Argument("mnBet", (double) GambleUtil.MINIMUM_BET);
			Argument maxBet = new Argument("mxBet", (double) GambleUtil.MAX_BET);
			Argument balanceArg = new Argument("balance", this.currentAmountToBetWith);
			Expression testBetAmountExpression = new Expression(this.betAmountExpression, leftScoreArg, rightScoreArg, minBet, maxBet, balanceArg);
			if(!testBetAmountExpression.checkSyntax() || !testBetAmountExpression.checkLexSyntax()) {
				log.warn("The syntax of the bet Amount expression {} is faulty with error: {}", this.betAmountExpression, testBetAmountExpression.getErrorMessage());
			}
		}
		
	}

}
