package fft_battleground.botland;

import java.util.List;
import java.util.Optional;
import java.util.TimerTask;
import java.util.Vector;

import org.apache.commons.lang3.tuple.Pair;
import org.thymeleaf.util.StringUtils;

import fft_battleground.botland.model.Bet;
import fft_battleground.botland.model.BetType;
import fft_battleground.event.BattleGroundEventBackPropagation;
import fft_battleground.event.model.BadBetEvent;
import fft_battleground.event.model.BetEvent;
import fft_battleground.event.model.BettingBeginsEvent;
import fft_battleground.event.model.BettingEndsEvent;
import fft_battleground.event.model.MatchInfoEvent;
import fft_battleground.event.model.TeamInfoEvent;
import fft_battleground.event.model.UnitInfoEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.model.ChatMessage;
import fft_battleground.repo.PlayerRecordRepo;
import fft_battleground.util.Router;

import lombok.Data;
import lombok.SneakyThrows;

@Data
public class BotLand extends TimerTask {
	private BotlandHelper helper;
	private String ircName;
	private boolean acceptingBets = true;
	
	//references
	protected Router<ChatMessage> chatMessageRouterRef;
	protected PlayerRecordRepo playerRecordRepo;
	protected BattleGroundEventBackPropagation battleGroundEventBackPropagationRef;
	
	//state data
	private BetterBetBot primaryBot;
	private List<BetterBetBot> subordinateBots;
	
	public BotLand(Integer currentAmountToBetWith, BettingBeginsEvent beginEvent, List<BetEvent> otherPlayerBets) {
		List<BetEvent> bets = new Vector<BetEvent>(otherPlayerBets);
		this.helper = new BotlandHelper(currentAmountToBetWith, beginEvent.getTeam1(), beginEvent.getTeam2(), bets);
	}

	@Override
	@SneakyThrows
	public void run() {
		Pair<List<BetEvent>, List<BetEvent>> betsBySide = this.helper.sortBetsBySide();
		this.primaryBot.setBetsBySide(betsBySide);
		this.primaryBot.setCurrentAmountToBetWith(helper.getCurrentAmountToBetWith());
		//get results from main bot
		Bet bet = primaryBot.call();
		//send results to irc
		this.sendBet(bet);
		
		//call subordinate bots
		
		
		//send all data to repo manager
		
	}
	
	public BetResultCollector createCollector() {
		BetResultCollector collector = new BetResultCollector(this.helper, this.primaryBot, this.subordinateBots);
		return collector;
	}
	
	public void addBet(BetEvent bet) {
		if(this.acceptingBets) {
			synchronized(this.helper.getOtherPlayerBets()) {
				Optional<BetEvent> preExistingEvent = this.helper.getOtherPlayerBets().stream().filter(betEvent -> StringUtils.equalsIgnoreCase(betEvent.getPlayer(), bet.getPlayer())).findFirst();
				if(preExistingEvent.isPresent()) {
					 this.helper.getOtherPlayerBets().remove(preExistingEvent.get());
				}
				 this.helper.getOtherPlayerBets().add(bet);
			}
		}
	}
	
	public void removeBet(BadBetEvent event) {
		synchronized(this.helper.getOtherPlayerBets()) {
			for(String player : ((BadBetEvent) event).getPlayers()) {
				for(BetEvent bet : this.helper.getOtherPlayerBets()) {
					if(bet.getPlayer().equals(player)) {
						this.helper.getOtherPlayerBets().remove(bet);
						break;
					}
				}
			}
		}
	}
	
	public void addTeamInfo(TeamInfoEvent event) {
		if(this.getHelper().getLeft() == event.getTeam()) {
			this.helper.addTeamInfo(event);
		} else if(this.getHelper().getRight() == event.getTeam()) {
			this.helper.addTeamInfo(event);
		}
		this.helper.addTeamInfo(event);
	}
	
	public void addUnitInfo(UnitInfoEvent event) {
		this.helper.addUnitInfo(event);
	}
	
	public void setCurrentAmountToBetWith(Integer currentAmountToBetWith) {
		this.helper.setCurrentAmountToBetWith(currentAmountToBetWith);
	}
	
	public void setBettingEndsEvent(BettingEndsEvent event) {
		this.helper.setBettingEndsEvent(event);
	}
	
	public void setMatchInfo(MatchInfoEvent event) {
		this.helper.setMatchInfo(event);
	}
	
	@SneakyThrows
	protected void sendBet(Bet bet) {
		String betString = String.format("!bet %1$s %2$s", BattleGroundTeam.getTeamName(bet.getTeam()), bet.getAmount());
		this.chatMessageRouterRef.sendDataToQueues(new ChatMessage(betString));
		this.battleGroundEventBackPropagationRef.SendUnitThroughTimer(new BetEvent(this.ircName, bet.getTeam(), bet.getAmount().toString(), bet.getAmount().toString(), BetType.VALUE));
	}
}
