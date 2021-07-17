package fft_battleground.botland.bot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Constant;
import org.mariuszgromada.math.mxparser.Expression;

import fft_battleground.botland.model.Bet;
import fft_battleground.botland.model.BotParam;
import fft_battleground.exception.BotConfigException;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.tournament.model.Unit;
import fft_battleground.util.GambleUtil;

public class UnitAwareBot extends BetterBetBot {

	private static final String BRAVE_MULTIPLIER_PARAMETER = "bravemultiplier";
	private static final String FAITH_MULTIPLIER_PARAMETER = "faithmuliplier";
	
	private String betAmountExpression;
	private String name;
	private float braveMultiplier = 0f;
	private float faithMultiplier = 0f;
	private Map<String, Float> unitParameters = new HashMap<>();
	
	public UnitAwareBot(Integer currentAmountToBetWith, BattleGroundTeam left, BattleGroundTeam right) {
		super(currentAmountToBetWith, left, right);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initParams(Map<String, BotParam> map) throws BotConfigException {
		if(map.containsKey(BET_AMOUNT_EXPRESSION_PARAMETER)) {
			this.betAmountExpression = map.get(BET_AMOUNT_EXPRESSION_PARAMETER).getValue();
		}
		if(map.containsKey(PERSONALITY_PARAM)) {
			this.personalityName = map.get(PERSONALITY_PARAM).getValue();
		}
		if(map.containsKey(INVERSE_PARAM)) {
			super.inverse = Boolean.valueOf(map.get(INVERSE_PARAM).getValue());
		}
		if(map.containsKey(BRAVE_MULTIPLIER_PARAMETER)) {
			this.braveMultiplier = Float.valueOf(map.get(BRAVE_MULTIPLIER_PARAMETER).getValue());
		}
		if(map.containsKey(FAITH_MULTIPLIER_PARAMETER)) {
			this.faithMultiplier = Float.valueOf(map.get(FAITH_MULTIPLIER_PARAMETER).getValue());
		}
		
		List<String> usedParameters = Arrays.asList(new String[] {BET_AMOUNT_EXPRESSION_PARAMETER, PERSONALITY_PARAM, INVERSE_PARAM, BRAVE_MULTIPLIER_PARAMETER, FAITH_MULTIPLIER_PARAMETER});
		Function<String, String> keyFunction = key -> StringUtils.lowerCase(key);
		this.unitParameters = map.keySet().stream()
				.filter(key -> !usedParameters.contains(key))
				.filter(key -> StringUtils.isNumeric(map.get(key).getValue()))
				.collect(Collectors.toMap(keyFunction, key -> Float.valueOf(map.get(key).getValue())));
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
		List<Unit> leftTeam = this.unitsBySide.getLeft();
		Float score = this.calculateScoreForTeam(leftTeam);
		return score;
	}

	@Override
	protected Float generateRightScore() {
		List<Unit> rightTeam = this.unitsBySide.getRight();
		Float score = this.calculateScoreForTeam(rightTeam);
		return score;
	}

	@Override
	protected Bet generateBetAmount(Float leftScore, Float rightScore, BattleGroundTeam chosenTeam) {
		Integer betAmount = this.calculateBetAmount(leftScore, rightScore);
		result = new Bet(chosenTeam, betAmount, this.isBotSubscriber);
		return result;
	}
	
	protected Float calculateScoreForTeam(List<Unit> units) {
		Float score = (float) units.stream().mapToDouble(unit -> this.calculateScoreForUnit(unit).doubleValue()).sum();
		return score;
	}
	
	protected Float calculateScoreForUnit(Unit unit) {
		Float score = 0f;
		score+= this.braveMultiplier * unit.getBrave();
		score += this.faithMultiplier * unit.getFaith();
		Integer attributeScore = unit.getUnitGeneAbilityElements().stream()
				.filter(ability -> this.unitParameters.containsKey(ability))
				.mapToInt(ability -> this.unitParameters.get(ability).intValue())
				.sum();
		score += attributeScore.floatValue();
		
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
