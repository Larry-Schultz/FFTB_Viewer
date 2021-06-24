package fft_battleground.botland.bot;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Constant;
import org.mariuszgromada.math.mxparser.Expression;

import com.fasterxml.jackson.databind.ObjectMapper;

import fft_battleground.botland.BetterBetBot;
import fft_battleground.botland.model.Bet;
import fft_battleground.botland.model.BotParam;
import fft_battleground.botland.model.ResultData;
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
	Map<String, Integer> geneMap;

	public GeneticBot(Integer currentAmountToBetWith, BattleGroundTeam left, BattleGroundTeam right) {
		super(currentAmountToBetWith, left, right);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initParams(Map<String, BotParam> map) {
		if(map.containsKey(BET_AMOUNT_EXPRESSION_PARAMETER)) {
			this.betAmountExpression = map.get(BET_AMOUNT_EXPRESSION_PARAMETER).getValue();
		}
		if(map.containsKey(GENE_FILE_PARAMETER)) {
			this.filename = map.get(GENE_FILE_PARAMETER).getValue();
		}
	}

	@Override
	public void init() {
		URL resourceUrl = this.getClass().getClassLoader().getResource(filename);
		ObjectMapper mapper = new ObjectMapper();
		try {
			this.genes = mapper.readValue(resourceUrl, ResultData.class);
		} catch (IOException e) {
			log.error("Error initializing gene bot from file {}", this.filename, e);
		}
		this.geneMap = GenericPairing.convertGenericPairListToMap(this.genes.getGeneticAttributes());
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
		Bet result = new Bet(chosenTeam, betAmount.intValue(), this.isBotSubscriber);
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
		
		result = Double.valueOf(exp.calculate()).intValue();
		
		return result;
	}

}
