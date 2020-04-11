package fft_battleground.bot;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import fft_battleground.bot.model.Bet;
import fft_battleground.bot.model.event.BetEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.model.ChatMessage;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.util.GambleUtil;
import fft_battleground.util.Router;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class DataBetBot extends BetBot {
	
	protected final String Name = "DataBetBot";
	
	public DataBetBot(Integer currentAmountToBetWith, List<BetEvent> otherPlayerBets, BattleGroundTeam left,
			BattleGroundTeam right) {
		super( currentAmountToBetWith, otherPlayerBets, left, right);
		// TODO Auto-generated constructor stub
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
	protected Integer generateBetAmount(Float leftScore, Float rightScore, BattleGroundTeam chosenTeam) {
		Float betAmount = GambleUtil.MINIMUM_BET.floatValue();
		if(chosenTeam == this.left) {
			float betRatio = leftScore / (leftScore + rightScore);
			betAmount = betRatio * (this.currentAmountToBetWith);
		} else {
			float betRatio = rightScore / (leftScore + rightScore);
			betAmount = betRatio * (this.currentAmountToBetWith);
		}
		
		return betAmount.intValue();
	}

	protected Float findScoreOfBets(List<BetEvent> bets) {
		float scoreSum = 0f;
		for(BetEvent bet: bets) {
			PlayerRecord playerRecord = this.playerBetRecords.get(bet.getPlayer());
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

}
