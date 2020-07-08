package fft_battleground.botland.bot;

import java.util.Map;

import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Expression;

import fft_battleground.botland.BetterBetBot;
import fft_battleground.botland.model.Bet;
import fft_battleground.botland.model.BetType;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.util.GambleUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BetCountBot extends BetterBetBot {

	private static final String BET_TYPE_PARAMETER = "betType";
	private static final String BET_AMOUNT_EXPRESSION_PARAMETER = "betExpression";
	
	public BetCountBot(Integer currentAmountToBetWith, BattleGroundTeam left,
			BattleGroundTeam right) {
		super(currentAmountToBetWith, left, right);
		// TODO Auto-generated constructor stub
	}

	private String name = "minBetBot";
	
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
	public void initParams(Map<String, String> parameters) {
		if(parameters.containsKey(BET_TYPE_PARAMETER)) {
			this.type = BetType.getBetType(parameters.get(BET_TYPE_PARAMETER));
		}
		if(parameters.containsKey(BET_AMOUNT_EXPRESSION_PARAMETER)) {
			this.betAmountExpression = parameters.get(BET_AMOUNT_EXPRESSION_PARAMETER);
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
		} else if(this.betAmountExpression != null) {
			Integer betAmount = this.calculateBetAmount(leftScore, rightScore);
			result = new Bet(chosenTeam, betAmount);
		}
		return result;
	}
	
	protected Integer calculateBetAmount(Float leftScore, Float rightScore) {
		Integer result = null;
		
		Argument leftScoreArg = new Argument("leftScore", leftScore);
		Argument rightScoreArg = new Argument("rightScore", rightScore);
		Argument minBet = new Argument("minBet", GambleUtil.MINIMUM_BET);
		Argument maxBet = new Argument("maxBet", GambleUtil.MAX_BET);
		
		Expression exp = new Expression(this.betAmountExpression, leftScoreArg, rightScoreArg, minBet, maxBet);
		
		result = new Double(exp.calculate()).intValue();
		
		return result;
	}

	@Override
	public void init() {
		if(this.betAmountExpression != null) {
			Expression testBetAmountExpression = new Expression(this.betAmountExpression, new Argument("leftScore", 0f), new Argument("rightScore", 0f));
			if(!testBetAmountExpression.checkSyntax()) {
				log.warn("The syntax of the bet Amount expression {} is faulty", this.betAmountExpression);
			}
		}
		
	}

}
