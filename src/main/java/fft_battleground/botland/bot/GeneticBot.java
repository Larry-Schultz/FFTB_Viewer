package fft_battleground.botland.bot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Constant;
import org.mariuszgromada.math.mxparser.Expression;

import fft_battleground.botland.GeneFileCache;
import fft_battleground.botland.model.Bet;
import fft_battleground.botland.model.BetType;
import fft_battleground.botland.model.BotParam;
import fft_battleground.botland.model.ResultData;
import fft_battleground.botland.personality.FactsPersonality;
import fft_battleground.exception.BotConfigException;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.tournament.model.Unit;
import fft_battleground.util.GambleUtil;
import fft_battleground.util.GenericPairing;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GeneticBot extends BetterBetBot {
	private static final String BET_AMOUNT_EXPRESSION_PARAMETER = "betExpression";
	private static final String GENE_FILE_PARAMETER = "geneFile";
	
	private String name;
	private String filename;
	protected String betAmountExpression;
	
	protected ResultData genes;
	protected Map<String, Integer> geneMap;
	protected GeneFileCache geneFileCache;

	public GeneticBot(Integer currentAmountToBetWith, BattleGroundTeam left, BattleGroundTeam right, GeneFileCache geneFileCache) {
		super(currentAmountToBetWith, left, right);
		super.personalityModule = new FactsPersonality();
		this.geneFileCache = geneFileCache;
	}

	@Override
	public void initParams(Map<String, BotParam> map) throws BotConfigException {
		if(map.containsKey(BET_AMOUNT_EXPRESSION_PARAMETER)) {
			this.betAmountExpression = map.get(BET_AMOUNT_EXPRESSION_PARAMETER).getValue();
		}
		if(map.containsKey(GENE_FILE_PARAMETER)) {
			this.filename = map.get(GENE_FILE_PARAMETER).getValue();
		}
		if(map.containsKey(PERSONALITY_PARAM)) {
			this.personalityName = map.get(PERSONALITY_PARAM).getValue();
		}
		if(map.containsKey(INVERSE_PARAM)) {
			this.inverse = Boolean.valueOf(map.get(INVERSE_PARAM).getValue());
		}
		
		this.genes = this.geneFileCache.getGeneData(this.filename);
	}

	@Override
	public void init() {
		
		this.geneMap = GenericPairing.convertGenericPairListToMap(this.genes.getGeneticAttributes());
		super.percentiles = GenericPairing.convertGenericPairListToMap(this.genes.getPercentiles());
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
		Float score = Float.valueOf(0f);
		List<Unit> leftSideUnits = this.unitsBySide.getLeft();
		List<String> attributesForSide = this.getAttributesForUnits(leftSideUnits);
		for(String attribute: attributesForSide) {
			if(this.geneMap.containsKey(attribute)) {
				score += (float) this.geneMap.get(attribute);
			}
		}
		
		return score;
	}

	@Override
	protected Float generateRightScore() {
		Float score = Float.valueOf(0f);
		List<Unit> rightSideUnits = this.unitsBySide.getRight();
		List<String> attributesForSide = this.getAttributesForUnits(rightSideUnits);
		for(String attribute: attributesForSide) {
			if(this.geneMap.containsKey(attribute)) {
				score += (float) this.geneMap.get(attribute);
			}
		}
		
		return score;
	}
	
	public List<String> getAttributesForUnits(List<Unit> units) {
		List<String> attributes = new ArrayList<>();
		for(Unit unit: units) {
			List<String> unitAttributes = unit.getUnitGeneAbilityElements();
			attributes.addAll(unitAttributes);
		}
		
		return attributes;
	}

	@Override
	protected Bet generateBetAmount(Float leftScore, Float rightScore, BattleGroundTeam chosenTeam) {
		Integer betAmount = this.calculateBetAmount(leftScore, rightScore);
		Bet result = null;
		if(betAmount != this.currentAmountToBetWith && betAmount != GambleUtil.getMinimumBetForBettor(this.isBotSubscriber)) {
			result = new Bet(chosenTeam, betAmount.intValue(), this.isBotSubscriber);
		} else if(betAmount == this.currentAmountToBetWith){
			result = new Bet(chosenTeam, BetType.ALLIN, this.isBotSubscriber);
		} else {
			result = new Bet(chosenTeam, BetType.FLOOR, this.isBotSubscriber);
		}

		return result;
	}
	
	protected Integer calculateBetAmount(Float leftScore, Float rightScore) {
		Integer result = null;
		
		Argument leftScoreArg = new Argument("leftScore", (double) leftScore);
		Argument rightScoreArg = new Argument("rightScore", (double) rightScore);
		Constant minBet = new Constant("mnBet", (double) GambleUtil.getMinimumBetForBettor(this.isBotSubscriber));
		Constant maxBet = new Constant("mxBet", (double) GambleUtil.MAX_BET);
		Argument balanceArg = new Argument("balance", this.currentAmountToBetWith);
		Argument percentileArg = new Argument("percentile", GambleUtil.calculatePercentile(leftScore, rightScore, super.percentiles));
		
		Expression exp = new Expression(this.betAmountExpression, leftScoreArg, rightScoreArg, minBet, maxBet, balanceArg, percentileArg);
		
		result = Double.valueOf(exp.calculate()).intValue();
		
		return result;
	}

}
