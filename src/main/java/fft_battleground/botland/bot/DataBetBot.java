package fft_battleground.botland.bot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import fft_battleground.botland.BetterBetBot;
import fft_battleground.botland.model.Bet;
import fft_battleground.botland.model.BotParam;
import fft_battleground.event.model.BetEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.util.GambleUtil;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataBetBot extends BetterBetBot {
	private static final String PLAYER_SCORE_EXPRESSION_PARAMETER = "playerScoreExpression";
	
	protected String Name = "DataBetBot";
	protected Map<String, PlayerRecord> playerBetRecords;
	
	private String playerScoreExpression;
	
	public DataBetBot(Integer currentAmountToBetWith, BattleGroundTeam left,
			BattleGroundTeam right) {
		super(currentAmountToBetWith, left, right);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void initParams(Map<String, BotParam> map) {
		if(map.containsKey(PLAYER_SCORE_EXPRESSION_PARAMETER)) {
			this.playerScoreExpression = map.get(PLAYER_SCORE_EXPRESSION_PARAMETER).getValue();
		}
		
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
		Float betAmount = GambleUtil.MINIMUM_BET.floatValue();
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
			Integer amount = GambleUtil.MINIMUM_BET;
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
		for(BetEvent betEvent : otherPlayerBets) {
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
		this.playerBetRecords = this.generatePlayerRecordMap(getOtherPlayerBets());
	}



}
