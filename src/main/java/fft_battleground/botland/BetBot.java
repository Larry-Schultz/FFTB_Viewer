package fft_battleground.botland;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import fft_battleground.botland.model.BattleGroundEventType;
import fft_battleground.botland.model.Bet;
import fft_battleground.botland.model.BetResults;
import fft_battleground.botland.model.BetType;
import fft_battleground.botland.model.TeamData;
import fft_battleground.event.BattleGroundEventBackPropagation;
import fft_battleground.event.model.BetEvent;
import fft_battleground.event.model.BettingEndsEvent;
import fft_battleground.event.model.MatchInfoEvent;
import fft_battleground.event.model.ResultEvent;
import fft_battleground.event.model.TeamInfoEvent;
import fft_battleground.event.model.UnitInfoEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.model.ChatMessage;
import fft_battleground.repo.PlayerRecordRepo;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.util.Router;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public abstract class BetBot extends TimerTask {
	protected Integer currentAmountToBetWith;
	protected List<BetEvent> otherPlayerBets;
	protected Map<String, PlayerRecord> playerBetRecords;
	protected BattleGroundTeam left;
	protected BattleGroundTeam right;
	
	protected Router<ChatMessage> chatMessageRouterRef;
	protected PlayerRecordRepo playerRecordRepo;
	protected BattleGroundEventBackPropagation battleGroundEventBackPropagationRef;
	
	protected BettingEndsEvent betEndEvent;
	protected MatchInfoEvent matchInfo;
	protected TeamData teamData;
	
	protected Pair<List<BetEvent>, List<BetEvent>> betsBySide;
	protected Bet result;
	
	private Date startTime;
	
	public BetBot() {
		this.teamData = new TeamData();
	}

	public BetBot(Integer currentAmountToBetWith, List<BetEvent> otherPlayerBets, BattleGroundTeam left, BattleGroundTeam right) {
		this.currentAmountToBetWith = currentAmountToBetWith;
		this.otherPlayerBets = otherPlayerBets;
		this.left = left;
		this.right = right;
		
		this.startTime = new Date();
		
		assert(this.currentAmountToBetWith.intValue() != 0);
		this.teamData = new TeamData();
	}
	
	public abstract String getName();
	protected abstract Float generateLeftScore();
	protected abstract Float generateRightScore();
	protected abstract Integer generateBetAmount(Float leftScore, Float rightScore, BattleGroundTeam chosenTeam);
	
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
	
	@Override
	public void run() {
		log.info("Running bot {}.", getName());
		this.setOtherPlayerBets(new Vector<>(this.otherPlayerBets));
		this.setBetsBySide(this.sortBetsBySide());
		this.generatePlayerRecordMap(this.otherPlayerBets);
		
		Bet bet = this.determineBet();
		this.setResult(bet);
		this.sendBet(result);
		
		log.info("BetBot {} thinks that the {} side will win", getName(), BattleGroundTeam.getTeamName(this.getResult().getTeam()));
		log.info("BetBot {} compared {} bets on the {} side, and {} bets on the {} side.  This took {} seconds.", getName(), 
				this.betsBySide.getLeft().size(), BattleGroundTeam.getTeamName(left),
				this.betsBySide.getRight().size(), BattleGroundTeam.getTeamName(right),
				this.timeToCalculateBet());
	}
	
	/**
	 * Generate an aftermath report based on bot data.  The winner returned here is based on data gleemed from changes in Balance data
	 * 
	 * @param type
	 * @return
	 */
	public BetResults getBetResult(BattleGroundEventType type) {
		BetResults results = null;
		if(type == BattleGroundEventType.WIN) {
			results = new BetResults(this.betsBySide, this.getLeftOrRightFromTeam(this.getResult().getTeam()), this.left, this.right, this.betEndEvent, 
					this.matchInfo, this.teamData);
		} else if(type == BattleGroundEventType.LOSS) {
			BattleGroundTeam winningTeam = null;
			BattleGroundTeam resultsTeam = this.getResult().getTeam();
			if(resultsTeam == this.left && resultsTeam != right) {
				winningTeam = BattleGroundTeam.RIGHT;
			} else if(resultsTeam != this.left && resultsTeam == right) {
				winningTeam = BattleGroundTeam.LEFT;
			}
			
			if(winningTeam != null) {
				results = new BetResults(this.betsBySide, winningTeam, this.left, this.right, this.betEndEvent, 
						this.matchInfo, this.teamData);
			}
		}
		return results;
	}
	
	public BetResults getBetResult(ResultEvent event) {
		BetResults results = null;
		results = new BetResults(this.betsBySide, event.getWinner(), this.left, this.right, this.betEndEvent, 
				this.matchInfo, this.teamData);
		
		return results;
	}
	
	/**
	 * Adds team event without caller having to know which team is which
	 * 
	 * @param event
	 */
	public void addTeamInfo(TeamInfoEvent event) {
		if(event.getTeam() == this.left) {
			this.getTeamData().setLeftTeamData(event);
		} else if(event.getTeam() == this.right) {
			this.getTeamData().setRightTeamData(event);
		}
		
		return;
	}
	
	public void addUnitInfo(UnitInfoEvent event) {
		this.getTeamData().addUnitInfo(event);
	}
	
	public BattleGroundTeam getLeftOrRightFromTeam(BattleGroundTeam team) {
		BattleGroundTeam result = null;
		if(team == this.left) {
			result = BattleGroundTeam.LEFT;
		} else if(team == this.right) {
			result =  BattleGroundTeam.RIGHT;
		} 
		
		return result;
		
	}
	
	//sort bets by left and right team
	public Pair<List<BetEvent>, List<BetEvent>> sortBetsBySide() {
		Pair<List<BetEvent>, List<BetEvent>> eventsBySide = new ImmutablePair<>(new Vector<BetEvent>(), new Vector<BetEvent>());
		for(BetEvent betEvent: this.otherPlayerBets) {
			if(betEvent.getTeam() == this.left || betEvent.getTeam() == BattleGroundTeam.LEFT) {
				eventsBySide.getLeft().add(betEvent);
			} else if(betEvent.getTeam() == this.right || betEvent.getTeam() == BattleGroundTeam.RIGHT) {
				eventsBySide.getRight().add(betEvent);
			}
		}
		
		this.betsBySide = eventsBySide;
		
		return eventsBySide;
	}
	
	public Map<String, PlayerRecord> generatePlayerRecordMap(List<BetEvent> otherPlayerBets) {
		Map<String, PlayerRecord> playerRecordMap = new HashMap<String, PlayerRecord>();
		for(BetEvent betEvent : otherPlayerBets) {
			Optional<PlayerRecord> maybePlayer = this.playerRecordRepo.findById(betEvent.getPlayer());
			if(maybePlayer.isPresent()) {
				playerRecordMap.put(betEvent.getPlayer(), maybePlayer.get());
			}
		}
		this.setPlayerBetRecords(playerRecordMap);
		return playerRecordMap;
	}
	
	@SneakyThrows
	protected void sendBet(Bet bet) {
		String betString = String.format("!bet %1$s %2$s", BattleGroundTeam.getTeamName(bet.getTeam()), bet.getAmount());
		this.chatMessageRouterRef.sendDataToQueues(new ChatMessage(betString));
		this.battleGroundEventBackPropagationRef.SendUnitThroughTimer(new BetEvent("datadrivenbot", bet.getTeam(), bet.getAmount().toString(), bet.getAmount().toString(), BetType.VALUE));
	}
	
	protected long timeToCalculateBet() {
		Date currentTime = new Date();
		
		long diffInMillies = Math.abs(currentTime.getTime() - this.startTime.getTime());
	    long diff = TimeUnit.SECONDS.convert(diffInMillies, TimeUnit.MILLISECONDS);
	    
	    return diff;
	}

	public void setBetEndEvent(BettingEndsEvent event) {
		this.betEndEvent = event;
		
	}
	
}
