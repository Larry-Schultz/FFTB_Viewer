package fft_battleground.botland.bot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Constant;
import org.mariuszgromada.math.mxparser.Expression;

import fft_battleground.botland.bot.genetic.GeneFileCache;
import fft_battleground.botland.bot.genetic.model.ResultData;
import fft_battleground.botland.bot.model.Bet;
import fft_battleground.botland.bot.model.BetType;
import fft_battleground.botland.bot.util.BetterBetBot;
import fft_battleground.botland.bot.util.BotCanInverse;
import fft_battleground.botland.bot.util.BotCanUseBetExpressions;
import fft_battleground.botland.bot.util.BotContainsPersonality;
import fft_battleground.botland.bot.util.BotParameterReader;
import fft_battleground.botland.bot.util.BotUsesGeneFile;
import fft_battleground.botland.model.BotParam;
import fft_battleground.botland.personality.FactsPersonality;
import fft_battleground.exception.BotConfigException;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.tournament.classifier.UnitAttributeClassifier;
import fft_battleground.tournament.classifier.V1UnitAttributeClassifier;
import fft_battleground.tournament.model.Unit;
import fft_battleground.util.GambleUtil;
import fft_battleground.util.GenericPairing;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GeneticBot 
extends BetterBetBot
implements BotCanInverse, BotContainsPersonality, BotCanUseBetExpressions, BotUsesGeneFile {
	private String name;
	private String filename;
	protected String betAmountExpression;
	
	protected ResultData genes;
	protected Map<String, Integer> geneMap;
	protected GeneFileCache<ResultData> geneFileCache;
	protected UnitAttributeClassifier V1UnitAttributeClassifier = new V1UnitAttributeClassifier();

	public GeneticBot(Integer currentAmountToBetWith, BattleGroundTeam left, BattleGroundTeam right, GeneFileCache<ResultData> geneFileCache) {
		super(currentAmountToBetWith, left, right);
		super.personalityModule = new FactsPersonality();
		this.geneFileCache = geneFileCache;
	}

	@Override
	public void initParams(Map<String, BotParam> map) throws BotConfigException {
		BotParameterReader reader = new BotParameterReader(map);
		this.filename = this.readGeneFileParameter(reader);
		this.betAmountExpression = this.readBetAmountExpression(reader)
				.orElseThrow(BotParameterReader.throwBotconfigException("missing parameter " + this.getBetAmountExpressionParameter() + " for bot " + this.name));
		this.personalityName = this.readPersonalityParam(reader);
		this.inverse = this.readInverseParameter(reader);
		
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
			List<String> unitAttributes = this.V1UnitAttributeClassifier.getUnitGeneAbilityElements(unit);
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
