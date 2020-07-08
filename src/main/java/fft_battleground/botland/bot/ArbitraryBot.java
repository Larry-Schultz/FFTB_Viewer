package fft_battleground.botland.bot;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.botland.BetterBetBot;
import fft_battleground.botland.model.Bet;
import fft_battleground.botland.model.BetType;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.util.GambleUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ArbitraryBot extends BetterBetBot {
	private static final Random random = new Random();
	
	private static final String BET_AMOUNT_PARAMETER = "betAmount";
	private static final String BET_TYPE_PARAMETER = "betType";
	private static final String CHOICE_PARAMETER = "choice";
	
	private Integer betAmount;
	private BetType betType;
	private BetChoice choice;
	
	private String name;
	
	protected BattleGroundTeam pickedTeam;
	
	public ArbitraryBot(Integer currentAmountToBetWith, BattleGroundTeam left, BattleGroundTeam right) {
		super(currentAmountToBetWith, left, right);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initParams(Map<String, String> map) {
		if(map.containsKey(BET_AMOUNT_PARAMETER)) {
			this.betAmount = Integer.valueOf(map.get(BET_AMOUNT_PARAMETER));
		}
		if(map.containsKey(BET_TYPE_PARAMETER)) {
			this.betType = BetType.getBetType(map.get(BET_TYPE_PARAMETER));
		}
		if(map.containsKey(CHOICE_PARAMETER)) {
			this.choice = BetChoice.getChoiceFromString(map.get(CHOICE_PARAMETER));
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
			bet = new Bet(chosenTeam, this.betType);
		} else if(this.betAmount != null) {
			bet = new Bet(chosenTeam, this.betAmount);
		} else if(this.betType == BetType.PERCENTAGE) { 
			bet = new Bet(chosenTeam, this.betAmount, this.betType); //if type is percentage, use amount as the percent
		} else {
			bet = new Bet(chosenTeam, GambleUtil.MINIMUM_BET);
		}
		
		return bet;
	}

	@Override
	public void init() {
		BetChoice tempChoice = this.choice;
		if(this.choice == null || this.choice == BetChoice.RANDOM) {
			List<BetChoice> choices = Arrays.asList(new BetChoice[] {BetChoice.LEFT, BetChoice.RIGHT});
			Integer nextIndex = ArbitraryBot.random.nextInt(choices.size());
			log.info("next index is: {}", nextIndex);
			BetChoice randomElement = choices.get(nextIndex);
			tempChoice = randomElement;
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
