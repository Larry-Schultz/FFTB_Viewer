package fft_battleground.botland.bot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Constant;
import org.mariuszgromada.math.mxparser.Expression;

import fft_battleground.botland.bot.exception.BotConfigException;
import fft_battleground.botland.bot.genetic.GeneFileV1Cache;
import fft_battleground.botland.bot.genetic.model.ResultData;
import fft_battleground.botland.bot.model.Bet;
import fft_battleground.botland.bot.util.BetterBetBot;
import fft_battleground.botland.bot.util.BotCanInverse;
import fft_battleground.botland.bot.util.BotCanUseBetExpressions;
import fft_battleground.botland.bot.util.BotContainsPersonality;
import fft_battleground.botland.bot.util.BotParameterReader;
import fft_battleground.botland.model.BotParam;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.tournament.classifier.UnitAttributeClassifier;
import fft_battleground.tournament.classifier.V1UnitAttributeClassifier;
import fft_battleground.tournament.model.Unit;
import fft_battleground.util.GambleUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UnitAwareBot 
extends BetterBetBot 
implements BotCanInverse, BotContainsPersonality, BotCanUseBetExpressions {

	private static final String BRAVE_MULTIPLIER_PARAMETER = "bravemultiplier";
	private static final String FAITH_MULTIPLIER_PARAMETER = "faithmuliplier";
	
	private String betAmountExpression;
	private String name;
	private float braveMultiplier = 0f;
	private float faithMultiplier = 0f;
	private Map<String, Float> unitParameters = new HashMap<>();
	private GeneFileV1Cache geneFileCache;
	
	protected UnitAttributeClassifier V1UnitAttributeClassifier = new V1UnitAttributeClassifier();
	
	public UnitAwareBot(Integer currentAmountToBetWith, BattleGroundTeam left, BattleGroundTeam right, GeneFileV1Cache geneFileCache) {
		super(currentAmountToBetWith, left, right);
		this.geneFileCache = geneFileCache;
	}

	@Override
	public void initParams(Map<String, BotParam> map) throws BotConfigException {
		BotParameterReader reader = new BotParameterReader(map);
		this.betAmountExpression = this.readBetAmountExpression(reader)
				.orElseThrow(BotParameterReader.throwBotconfigException("missing parameter " + this.getBetAmountExpressionParameter() + " for bot " + this.name));
		this.personalityName = this.readPersonalityParam(reader);
		this.inverse = this.readInverseParameter(reader);
		this.braveMultiplier = reader.readParam(BRAVE_MULTIPLIER_PARAMETER, Float::valueOf).orElse(0f);
		this.faithMultiplier = reader.readParam(FAITH_MULTIPLIER_PARAMETER, Float::valueOf).orElse(0f);
		
		List<String> usedParameters = Arrays.asList(new String[] {this.getBetAmountExpressionParameter(), this.getPersonalityParam(), this.getInverseParam(), 
				BRAVE_MULTIPLIER_PARAMETER, FAITH_MULTIPLIER_PARAMETER});
		Function<String, Float> readParameterFunction = (key) -> reader.readParam(key, Float::valueOf).orElse(0f);
		this.unitParameters = map.keySet().stream()
				.filter(key -> !usedParameters.contains(key))
				.filter(key -> StringUtils.isNumeric(map.get(key).getValue()))
				.collect(Collectors.toMap(StringUtils::lowerCase, readParameterFunction));
		
		
	}

	@Override
	public void init() throws BotConfigException {
		UnitAwareBotConfigVerifier verifier = new UnitAwareBotConfigVerifier(this.geneFileCache);
		Optional<List<String>> invalidAttributes = verifier.checkForInvalidAttributes(this.unitParameters.keySet());
		if(invalidAttributes.isPresent()) {
			String invalidAttributeString = StringUtils.join(invalidAttributes.get(), ", ");
			log.warn("UnitAwareBot {} has the invalid attributes: {}", this.getName(), invalidAttributeString);
		}
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
		List<String> geneAbilities = this.V1UnitAttributeClassifier.getUnitGeneAbilityElements(unit)
				.stream().map(ability -> StringUtils.lowerCase(ability)).collect(Collectors.toList());
		Map<String, Float> relevantGeneAbilities = new HashMap<>();
		for(String ability: geneAbilities) {
			if(this.unitParameters.containsKey(ability)) {
				relevantGeneAbilities.put(ability, this.unitParameters.get(ability));
			}
		}
		Double attributeScore = relevantGeneAbilities.keySet().stream()
				.mapToDouble(ability -> relevantGeneAbilities.get(ability))
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
		
		result = Double.valueOf(exp.calculate()).intValue();
		
		return result;
	}

}

class UnitAwareBotConfigVerifier {
	
	private List<String> validAttributes;
	
	public UnitAwareBotConfigVerifier() {}
	
	public UnitAwareBotConfigVerifier(GeneFileV1Cache cache) throws BotConfigException {
		try {
		ResultData latestFile = cache.getLatestFile();
		this.validAttributes = latestFile.getGeneticAttributes().stream()
			.map(geneticAttribute -> StringUtils.lowerCase(geneticAttribute.getKey()))
			.collect(Collectors.toList());
		} catch(NullPointerException e) {
			throw new BotConfigException("Missing or bad Gen 1 gene file", e);
		}
	}
	
	public Optional<List<String>> checkForInvalidAttributes(Collection<String> attributes) {
		List<String> invalidAttributes = new ArrayList<>();
		for(String attribute: attributes) {
			if(!this.validAttributes.contains(StringUtils.lowerCase(attribute))) {
				invalidAttributes.add(attribute);
			}
		}
		
		Optional<List<String>> result = Optional.of(invalidAttributes);
		if(invalidAttributes.size() == 0) {
			result = Optional.empty();
		}
		
		return result;
	}
}
