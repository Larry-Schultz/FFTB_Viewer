package fft_battleground.botland.bot;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Constant;
import org.mariuszgromada.math.mxparser.Expression;

import fft_battleground.botland.model.Bet;
import fft_battleground.botland.model.BotParam;
import fft_battleground.botland.personality.model.BraveFaith;
import fft_battleground.exception.BotConfigException;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.tournament.model.Unit;
import fft_battleground.util.GambleUtil;

public class BraveFaithBot extends BetterBetBot {

	private static final String BET_AMOUNT_EXPRESSION_PARAMETER = "betExpression";
	private static final String BRAVE_OR_FAITH = "bravefaith";
	
	private String betAmountExpression;
	private BraveFaith braveFaith;
	private String name;
	
	public BraveFaithBot(Integer currentAmountToBetWith, BattleGroundTeam left, BattleGroundTeam right) {
		super(currentAmountToBetWith, left, right);
		
	}
	
	@Override
	public void initParams(Map<String, BotParam> map) throws BotConfigException {
		if(map.containsKey(BRAVE_OR_FAITH)) {
			this.braveFaith = BraveFaith.parseString(map.get(BRAVE_OR_FAITH).getValue());
		}
		if(map.containsKey(BET_AMOUNT_EXPRESSION_PARAMETER)) {
			this.betAmountExpression = map.get(BET_AMOUNT_EXPRESSION_PARAMETER).getValue();
		}
		if(map.containsKey(PERSONALITY_PARAM)) {
			super.personalityName = map.get(PERSONALITY_PARAM).getValue();
		}
		if(map.containsKey(INVERSE_PARAM)) {
			super.inverse = Boolean.valueOf(map.get(INVERSE_PARAM).getValue());
		}
		
		if(this.braveFaith == null) {
			throw new BotConfigException("missing parameter bravefaith for bot " + this.name);
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
		List<Unit> units = this.unitsBySide.getLeft();
		Float score = this.generateScoreForUnits(units);
		return score;
	}

	@Override
	protected Float generateRightScore() {
		List<Unit> units = this.unitsBySide.getRight();
		Float score = this.generateScoreForUnits(units);
		
		return score;
	}

	@Override
	protected Bet generateBetAmount(Float leftScore, Float rightScore, BattleGroundTeam chosenTeam) {
		Integer betAmount = this.calculateBetAmount(leftScore, rightScore);
		result = new Bet(chosenTeam, betAmount, this.isBotSubscriber);
		return result;
	}

	protected Float generateScoreForUnits(List<Unit> units) {
		Float score = 0f;
		if(this.braveFaith == BraveFaith.BRAVE) {
			score = (float) units.stream().mapToInt(unit -> Optional.ofNullable(unit.getBrave()).orElse((short) 0)).sum();
		} else if(this.braveFaith == BraveFaith.FAITH) {
			score = (float) units.stream().mapToInt(unit -> Optional.ofNullable(unit.getFaith()).orElse((short) 0)).sum();
		} else if(this.braveFaith == BraveFaith.BOTH) {
			score = (float) units.stream().mapToInt(unit -> Optional.ofNullable(unit.getBrave()).orElse((short) 0) + Optional.ofNullable(unit.getFaith()).orElse((short) 0)).sum();
		}
		return score;
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
