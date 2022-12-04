package fft_battleground.botland.bot;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import fft_battleground.botland.bot.exception.BotConfigException;
import fft_battleground.botland.bot.model.Bet;
import fft_battleground.botland.bot.util.BetterBetBot;
import fft_battleground.botland.bot.util.BotCanInverse;
import fft_battleground.botland.bot.util.BotContainsPersonality;
import fft_battleground.botland.bot.util.BotParameterReader;
import fft_battleground.botland.model.BotParam;
import fft_battleground.event.detector.model.BetEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.util.GambleUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReferenceBot 
extends BetterBetBot
implements BotCanInverse, BotContainsPersonality{

	private static final String REFERENCE_PLAYER_PARAMETER = "reference_player";
	private static final Float NOT_REFERENCE_PLAYER_SIDE_SCORE = 0f;
	private static final int NO_BET_DEFAULT_VALUE = 1;
	
	private String name;
	private String referencePlayer;
	
	private PlayerRecord referencePlayerRecord;
	private BattleGroundTeam referencePlayerSide;
	private Integer referencePlayerBetAmount;
	
	public ReferenceBot(Integer currentAmountToBetWith, BattleGroundTeam left, BattleGroundTeam right) {
		super(currentAmountToBetWith, left, right);
	}

	@Override
	public void initParams(Map<String, BotParam> map) throws BotConfigException {
		BotParameterReader reader = new BotParameterReader(map);
		if(map.containsKey(REFERENCE_PLAYER_PARAMETER)) {
			this.referencePlayer = map.get(REFERENCE_PLAYER_PARAMETER).getValue();
		}
		this.personalityName = this.readPersonalityParam(reader);
		this.inverse = this.readInverseParameter(reader);

	}

	@Override
	public void init() {
		Optional<BetEvent> possibleBetEventForReferencePlayerOnLeft = this.betsBySide.getLeft().stream().filter(this::containsReferencePlayer).findFirst();
		Optional<BetEvent> possibleBetEventForReferencePlayerOnRight = this.betsBySide.getRight().stream().filter(this::containsReferencePlayer).findFirst();
		
		if(possibleBetEventForReferencePlayerOnLeft.isPresent()) {
			this.referencePlayerSide = BattleGroundTeam.LEFT;
			this.referencePlayerBetAmount = possibleBetEventForReferencePlayerOnLeft.get().getBetAmountInteger();
			this.referencePlayerRecord = possibleBetEventForReferencePlayerOnLeft.get().getMetadata();
		} else if(possibleBetEventForReferencePlayerOnRight.isPresent()) {
			this.referencePlayerSide = BattleGroundTeam.RIGHT;
			this.referencePlayerBetAmount = possibleBetEventForReferencePlayerOnRight.get().getBetAmountInteger();
			this.referencePlayerRecord = possibleBetEventForReferencePlayerOnRight.get().getMetadata();
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
		Float score = this.referencePlayerSide == BattleGroundTeam.LEFT ? this.referencePlayerBetAmount : NOT_REFERENCE_PLAYER_SIDE_SCORE;
		return score;
	}

	@Override
	protected Float generateRightScore() {
		Float score = this.referencePlayerSide == BattleGroundTeam.RIGHT ? this.referencePlayerBetAmount : NOT_REFERENCE_PLAYER_SIDE_SCORE;
		return score;
	}

	@Override
	protected Bet generateBetAmount(Float leftScore, Float rightScore, BattleGroundTeam chosenTeam) {
		int referencePlayerBetAmount = 0;
		if(this.referencePlayerBetAmount == null) {
			log.warn("no bet found for player {}", this.referencePlayer);
			referencePlayerBetAmount = NO_BET_DEFAULT_VALUE;
		} else {
			referencePlayerBetAmount = this.referencePlayerBetAmount.intValue();
		}
		Integer betAmount = NumberUtils.min(referencePlayerBetAmount, this.currentAmountToBetWith, GambleUtil.MAX_BET);
		boolean isSubscriber = true;
		if(this.referencePlayerRecord != null && this.referencePlayerRecord.getIsSubscriber() != null) {
			isSubscriber = this.referencePlayerRecord.getIsSubscriber();
		}
		Bet bet = new Bet(chosenTeam, betAmount, isSubscriber);
		return bet;
	}
	
	protected boolean containsReferencePlayer(BetEvent betEvent) {
		boolean result = StringUtils.equalsIgnoreCase(this.referencePlayer, betEvent.getPlayer());
		return result;
	}

}
