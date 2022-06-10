package fft_battleground.botland.bot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import fft_battleground.botland.bot.model.Bet;
import fft_battleground.botland.bot.util.BetterBetBot;
import fft_battleground.botland.bot.util.BotCanInverse;
import fft_battleground.botland.bot.util.BotContainsPersonality;
import fft_battleground.botland.bot.util.BotParameterReader;
import fft_battleground.botland.model.BotParam;
import fft_battleground.event.detector.model.BetEvent;
import fft_battleground.exception.BotConfigException;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.util.GambleUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataBetBot 
extends BetterBetBot 
implements BotContainsPersonality, BotCanInverse {
	protected String Name = "DataBetBot";
	protected Map<String, PlayerRecord> playerBetRecords;
	
	public DataBetBot(Integer currentAmountToBetWith, BattleGroundTeam left,
			BattleGroundTeam right) {
		super(currentAmountToBetWith, left, right);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void initParams(Map<String, BotParam> map) throws BotConfigException {
		BotParameterReader reader = new BotParameterReader(map);
		this.personalityName = this.readPersonalityParam(reader);
		this.inverse = this.readInverseParameter(reader);
		
	}

	@Override
	public void setName(String name) {
		this.Name = name;
	}
	
	@Override
	public String getName() {
		return this.Name;
	}

	@Override
	protected Float generateLeftScore() {
		Float score = this.findScoreOfBets(this.betsBySide.getLeft());
		return score;
	}

	@Override
	protected Float generateRightScore() {
		Float score = this.findScoreOfBets(this.betsBySide.getRight());
		return score;
	}

	@Override
	protected Bet generateBetAmount(Float leftScore, Float rightScore, BattleGroundTeam chosenTeam) {
		Float betAmount = GambleUtil.getMinimumBetForBettor(this.isBotSubscriber).floatValue();
		if(chosenTeam == this.left) {
			float betRatio = leftScore / (leftScore + rightScore);
			betAmount = betRatio * (this.currentAmountToBetWith);
		} else {
			float betRatio = rightScore / (leftScore + rightScore);
			betAmount = betRatio * (this.currentAmountToBetWith);
		}
		
		Bet result = new Bet(chosenTeam, betAmount.intValue(), this.isBotSubscriber);
		
		return result;
	}

	protected Float findScoreOfBets(List<BetEvent> bets) {
		float scoreSum = 0f;
		for(BetEvent bet: bets) {
			PlayerRecord playerRecord = null;
			try {
				playerRecord = this.playerBetRecords.get(bet.getPlayer());
			}catch(NullPointerException e) {
				if(this.playerBetRecords == null) {
					log.error("this*.playerBetRecords is null");
				}
				if(bet == null) {
					log.error("the current bet is null");
				}
				
			}
			Integer amount = GambleUtil.getMinimumBetForBettor(this.isBotSubscriber);
			if(playerRecord != null) {
				amount = GambleUtil.getBetAmountFromBetString(playerRecord, bet);
				scoreSum += this.scoreByPlayer(playerRecord.getWins(), playerRecord.getLosses(), amount, playerRecord.getLastKnownAmount());
			}
			
		}
		
		return scoreSum;
	}
	
	protected float scoreByPlayer(Integer wins, Integer losses, Integer betAmount, Integer totalAmountPlayer) {
		float score = 1f;
		if(betAmount != null) {
			float winLossRatio = 1f;
			if(wins != null && losses != null) {
				winLossRatio = (float) (wins + 1)/(losses + 1);
			}
			
			float betRatio = 1f;
		    if(totalAmountPlayer != null) { 
		    	betRatio = (float) (betAmount.floatValue() + 1) /(totalAmountPlayer.floatValue() + 1); 
		    }
			  
			score = betAmount.floatValue() * winLossRatio * betRatio;
		}
		return score;
	}
	
	protected Map<String, PlayerRecord> generatePlayerRecordMap(List<BetEvent> otherPlayerBets) {
		Map<String, PlayerRecord> playerRecordMap = new HashMap<String, PlayerRecord>();
		
		for(int i = 0; i < otherPlayerBets.size(); i++) {
			BetEvent betEvent = otherPlayerBets.get(i);
			Optional<PlayerRecord> maybePlayer = this.playerRecordRepoRef.findById(betEvent.getPlayer());
			if(maybePlayer.isPresent()) {
				playerRecordMap.put(betEvent.getPlayer(), maybePlayer.get());
			}
		}
		this.playerBetRecords = playerRecordMap;
		return playerRecordMap;
	}

	@Override
	public void init() {
		List<BetEvent> otherPlayerBets = Collections.synchronizedList(getOtherPlayerBets());
		List<BetEvent> otherPlayerBetsCopy = new ArrayList<>(otherPlayerBets);
		this.playerBetRecords = this.generatePlayerRecordMap(otherPlayerBetsCopy);
	}

}
