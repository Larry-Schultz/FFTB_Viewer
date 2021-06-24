package fft_battleground.botland;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.tuple.Pair;

import fft_battleground.botland.model.Bet;
import fft_battleground.botland.model.BetType;
import fft_battleground.botland.model.BotParam;
import fft_battleground.botland.model.TeamData;
import fft_battleground.event.detector.model.BetEvent;
import fft_battleground.event.detector.model.MatchInfoEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.repo.repository.PlayerRecordRepo;
import fft_battleground.tournament.model.Unit;
import fft_battleground.util.GambleUtil;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public abstract class BetterBetBot
implements Callable<Bet> {
	protected boolean isBotSubscriber = true;
	
	protected Integer currentAmountToBetWith;
	protected List<BetEvent> otherPlayerBets;
	protected BattleGroundTeam left;
	protected BattleGroundTeam right;

	protected MatchInfoEvent matchInfo;
	protected TeamData teamData;
	protected PlayerRecordRepo playerRecordRepoRef;
	
	protected Pair<List<BetEvent>, List<BetEvent>> betsBySide;
	protected Pair<List<Unit>, List<Unit>> unitsBySide;
	protected Bet result;
	
	protected Date startTime;
	protected String dateFormat;
	
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
		this.init();
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
		Bet result = this.generateBetAmount(leftScore, rightScore, winningTeam);
		
		return result;
	}
	
	protected void ensureBetIsWithinParameters(Bet bet) {
		if(bet == null) {
			//bet = new Bet(this.left, GambleUtil.MINIMUM_BET);
			log.warn("The bot {} provided a null bet", this.getName());
		} else if (bet.getType() == BetType.VALUE) {
			if(bet.getAmount() == null) {
				bet.setAmount(GambleUtil.getMinimumBetForBettor(this.isBotSubscriber));
			} else if (bet.getAmount() > GambleUtil.MAX_BET) {
				log.warn("The bot {} provided a bet over the maximum value", this.getName());
				bet.setAmount(GambleUtil.MAX_BET);
			} else if (bet.getAmount() < GambleUtil.getMinimumBetForBettor(this.isBotSubscriber)) {
				log.warn("The bot {} provided a bet under the minimum value", this.getName());
				bet.setAmount(GambleUtil.getMinimumBetForBettor(this.isBotSubscriber));
			}
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
		log.info("Bot {}: The leftScore is {} and the rightScore is {}", this.getName(), leftScore, rightScore);
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

	public abstract void initParams(Map<String, BotParam> map);
	
	public abstract void init();
	
	public abstract String getName();
	
	public abstract void setName(String name);

	protected abstract Float generateLeftScore();

	protected abstract Float generateRightScore();

	protected abstract Bet generateBetAmount(Float leftScore, Float rightScore, BattleGroundTeam chosenTeam);
}
