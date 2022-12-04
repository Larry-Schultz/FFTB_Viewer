package fft_battleground.botland.bot;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.botland.bot.exception.BotConfigException;
import fft_battleground.botland.bot.model.Bet;
import fft_battleground.botland.bot.model.BetType;
import fft_battleground.botland.bot.util.BetterBetBot;
import fft_battleground.botland.bot.util.BotCanInverse;
import fft_battleground.botland.bot.util.BotContainsPersonality;
import fft_battleground.botland.bot.util.BotParameterReader;
import fft_battleground.botland.model.BotParam;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.util.GambleUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ArbitraryBot 
extends BetterBetBot
implements BotCanInverse, BotContainsPersonality {
	private static final Random random = new Random();
	
	private static final String BET_AMOUNT_PARAMETER = "betAmount";
	private static final String BET_TYPE_PARAMETER = "betType";
	private static final String CHOICE_PARAMETER = "choice";
	
	private Optional<Integer> betAmount;
	private BetType betType;
	private Optional<BetChoice> choice;
	
	private String name;
	
	protected BattleGroundTeam pickedTeam;
	
	public ArbitraryBot(Integer currentAmountToBetWith, BattleGroundTeam left, BattleGroundTeam right) {
		super(currentAmountToBetWith, left, right);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initParams(Map<String, BotParam> map) throws BotConfigException {
		BotParameterReader reader = new BotParameterReader(map);
		this.betAmount = reader.readParam(BET_AMOUNT_PARAMETER, Integer::valueOf);
		this.betType = reader.readParam(BET_TYPE_PARAMETER, BetType::getBetType)
				.orElseThrow(reader.throwBotconfigException("Missing Bet Type"));
		this.choice = reader.readParam(CHOICE_PARAMETER, BetChoice::getChoiceFromString);
		this.personalityName = this.readPersonalityParam(reader);
		this.inverse = this.readInverseParameter(reader);
		
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
		Float result = 0f;
		if(this.pickedTeam == BattleGroundTeam.LEFT || this.pickedTeam == this.left) {
			result = 1f;
		}
		
		return result;
	}

	@Override
	protected Float generateRightScore() {
		Float result = 0f;
		if(this.pickedTeam == BattleGroundTeam.RIGHT || this.pickedTeam == this.right) {
			result = 1f;
		}
		
		return result;
	}

	@Override
	protected Bet generateBetAmount(Float leftScore, Float rightScore, BattleGroundTeam chosenTeam) {
		Bet bet = null;
		if(this.betType == BetType.FLOOR || this.betType == BetType.ALLIN) {
			bet = new Bet(chosenTeam, this.betType, this.isBotSubscriber);
		} else if(this.betAmount.isPresent()) {
			bet = new Bet(chosenTeam, this.betAmount.get(), this.isBotSubscriber);
		} else if(this.betType == BetType.PERCENTAGE && this.betAmount.isPresent()) { 
			bet = new Bet(chosenTeam, this.betAmount.get(), this.betType, this.isBotSubscriber); //if type is percentage, use amount as the percent
		} else if(this.betType == BetType.RANDOM) {
			Integer randomValue = random.nextInt(Math.min(this.currentAmountToBetWith, GambleUtil.MAX_BET)) + GambleUtil.getMinimumBetForBettor(this.isBotSubscriber);
			randomValue = Math.min(randomValue, this.currentAmountToBetWith);
			bet = new Bet(chosenTeam, randomValue, this.isBotSubscriber);
		} else {
			bet = new Bet(chosenTeam, GambleUtil.getMinimumBetForBettor(this.isBotSubscriber), this.isBotSubscriber);
		}
		
		return bet;
	}

	@Override
	public void init() {
		BetChoice tempChoice = null;
		if(this.choice.isEmpty() || (this.choice.isPresent() && this.choice.get() == BetChoice.RANDOM)) {
			List<BetChoice> choices = Arrays.asList(new BetChoice[] {BetChoice.LEFT, BetChoice.RIGHT});
			Integer nextIndex = ArbitraryBot.random.nextInt(choices.size());
			log.info("next index is: {}", nextIndex);
			BetChoice randomElement = choices.get(nextIndex);
			tempChoice = randomElement;
		} else {
			tempChoice = this.choice.get();
		}
		
		BattleGroundTeam pickedTeam = BattleGroundTeam.RANDOM;
		if(tempChoice == BetChoice.LEFT) {
			this.pickedTeam = this.left;
		} else if(tempChoice == BetChoice.RIGHT) {
			this.pickedTeam = this.right;
		}
	}
	

}

enum BetChoice {
	LEFT("left"),
	RIGHT("right"),
	RANDOM("random")
	;
	
	private String str;
	
	private BetChoice(String str) {
		this.str = str;
	}
	
	public String getString() {
		return this.str;
	}
	
	public static BetChoice getChoiceFromString(String parameter) {
		BetChoice result = null;
		for(BetChoice choice : BetChoice.values()) {
			if(StringUtils.equalsIgnoreCase(choice.getString(), parameter)) {
				result = choice;
				break;
			}
		}
		
		return result;
	}
}
