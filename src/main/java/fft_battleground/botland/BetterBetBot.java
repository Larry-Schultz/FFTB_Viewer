package fft_battleground.botland;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.tuple.Pair;

import fft_battleground.botland.model.Bet;
import fft_battleground.botland.model.TeamData;
import fft_battleground.event.model.BetEvent;
import fft_battleground.event.model.MatchInfoEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.repo.PlayerRecordRepo;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.util.GambleUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public abstract class BetterBetBot
implements Callable<Bet> {
	protected Integer currentAmountToBetWith;
	protected List<BetEvent> otherPlayerBets;
	protected Map<String, PlayerRecord> playerBetRecords;
	protected BattleGroundTeam left;
	protected BattleGroundTeam right;

	protected MatchInfoEvent matchInfo;
	protected TeamData teamData;
	protected PlayerRecordRepo playerRecordRepoRef;
	
	protected Pair<List<BetEvent>, List<BetEvent>> betsBySide;
	protected Bet result;
	
	protected Date startTime;
	
	public BetterBetBot(Integer currentAmountToBetWith, BattleGroundTeam left, BattleGroundTeam right) {
		this.currentAmountToBetWith = currentAmountToBetWith;
		this.left = left;
		this.right = right;
		
		this.startTime = new Date();
		
		assert(this.currentAmountToBetWith.intValue() != 0);
		this.teamData = new TeamData();
	}

	@Override
	public Bet call() throws Exception {
		// TODO Auto-generated method stub
		Bet bet =  this.determineBet();
		this.ensureBetIsWithinParameters(bet);
		this.result = bet;
		return bet;
	}
	
	/**
	 * Calculate a bet using two scoring algorithms.
	 * 
	 * @return
	 */
	protected Bet determineBet() {
		Float leftScore = this.generateLeftScore();
		Float rightScore = this.generateRightScore();
		
		BattleGroundTeam winningTeam = this.determineWinningTeam(leftScore, rightScore);
		Integer betAmount = this.generateBetAmount(leftScore, rightScore, winningTeam);
		
		Bet result = new Bet(winningTeam, betAmount);
		return result;
	}
	
	protected void ensureBetIsWithinParameters(Bet bet) {
		if(bet.getAmount() > GambleUtil.MAX_BET) {
			bet.setAmount(GambleUtil.MAX_BET);
		} else if(bet.getAmount() < GambleUtil.MINIMUM_BET) {
			bet.setAmount(GambleUtil.MINIMUM_BET);
		}
	}
	
	/**
	 * Uses scores to determine a winner
	 * 
	 * @param leftScore
	 * @param rightScore
	 * @return
	 */
	protected BattleGroundTeam determineWinningTeam(Float leftScore, Float rightScore) {
		log.info("The leftScore is {} and the rightScore is {}", leftScore, rightScore);
		BattleGroundTeam winningTeam = null;
		if(leftScore > rightScore) {
			winningTeam = this.left;
		} else if(leftScore < rightScore) {
			winningTeam = this.right;
		} else {
			winningTeam = this.left; //just pick one
		}
		
		return winningTeam;
	}

	public abstract String getName();

	protected abstract Float generateLeftScore();

	protected abstract Float generateRightScore();

	protected abstract Integer generateBetAmount(Float leftScore, Float rightScore, BattleGroundTeam chosenTeam);
}
