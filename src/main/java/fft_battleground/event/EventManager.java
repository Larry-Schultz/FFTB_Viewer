package fft_battleground.event;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import fft_battleground.botland.BetBot;
import fft_battleground.botland.BetBotFactory;
import fft_battleground.botland.bot.OddsBot;
import fft_battleground.botland.model.BetResults;
import fft_battleground.botland.model.DatabaseResultsData;
import fft_battleground.dump.DumpService;
import fft_battleground.event.model.BadBetEvent;
import fft_battleground.event.model.BalanceEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BetEvent;
import fft_battleground.event.model.BetInfoEvent;
import fft_battleground.event.model.BettingBeginsEvent;
import fft_battleground.event.model.BettingEndsEvent;
import fft_battleground.event.model.MatchInfoEvent;
import fft_battleground.event.model.ResultEvent;
import fft_battleground.event.model.TeamInfoEvent;
import fft_battleground.event.model.UnitInfoEvent;
import fft_battleground.model.ChatMessage;
import fft_battleground.util.Router;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EventManager extends Thread {
	
	@Autowired
	private BlockingQueue<BattleGroundEvent> eventManagerQueue;
	
	@Autowired
	private Router<ChatMessage> messageSenderRouter;
	
	@Autowired
	private Router<DatabaseResultsData> betResultsRouter;
	
	@Autowired
	private BetBotFactory betBotFactory;
	
	private Timer bettingTimer = new Timer();
	protected long bettingDelay = 33 * 1000;
	
	public EventManager() {
		this.setName("EventManagerThread");
	}
	
	@Override
	public void run() {
		int currentAmount = 1;
		List<BetEvent> currentBets = new Vector<>();
		boolean bettingCurrently = false;
		BetBot previousBetBot = null;
		BetBot currentBetBot = null;
		while(true) {
			try {
				BattleGroundEvent event = this.eventManagerQueue.take();
				switch(event.getEventType()) {
					case FIGHT_BEGINS:
						this.handleFight(event);
						break;
					case BETTING_BEGINS:
						bettingCurrently = true;
						sendBalanceRequest(); //start the WIN/LOSS chain of events
						bettingCurrently = true;
						currentBets = new Vector<>();
						if(event instanceof BettingBeginsEvent) {
							BettingBeginsEvent beginEvent = (BettingBeginsEvent) event;
							previousBetBot = currentBetBot;
							currentBetBot = this.betBotFactory.create(OddsBot.class, currentAmount, currentBets, beginEvent);
							this.bettingTimer.schedule(currentBetBot, bettingDelay);
						}
						break;
					case BETTING_ENDS:
						bettingCurrently = false;
						if(currentBetBot != null) {
							currentBetBot.setBetEndEvent((BettingEndsEvent) event);
						}
						//this.sendScheduledMessage("!match", 5000L);
						break;
					case BALANCE:
						BalanceEvent balanceEvent = (BalanceEvent) event;
						currentAmount = balanceEvent.getAmount();
						if(currentBetBot != null) {
							currentBetBot.setCurrentAmountToBetWith(currentAmount);
						}
						break;
					case BET:
						if(bettingCurrently && event instanceof BetEvent) {
							this.addToBets(currentBets, (BetEvent) event);
						}
						break;
					case BET_INFO:
						if(event instanceof BetInfoEvent) {
							BetInfoEvent betInfoEvent = (BetInfoEvent) event;
							if(betInfoEvent.getTeam() != null) {
								this.addToBets(currentBets, new BetEvent(betInfoEvent));
							}
						}
						break;
					case BAD_BET:
						if(bettingCurrently && event instanceof BadBetEvent) {
							for(String player : ((BadBetEvent) event).getPlayers()) {
								for(BetEvent bet : currentBets) {
									if(bet.getPlayer().equals(player)) {
										currentBets.remove(bet);
										break;
									}
								}
							}
						}
						break;
					case RESULT:
						if(currentBetBot != null && event instanceof ResultEvent) {
							BetResults aftermathOfBotBet = currentBetBot.getBetResult((ResultEvent)event);
							this.betResultsRouter.sendDataToQueues(aftermathOfBotBet);
						}
						break;
					case OTHER_PLAYER_BALANCE: case LEVEL_UP: case OTHER_PLAYER_EXP: case ALLEGIANCE:
					case PLAYER_SKILL: case BUY_SKILL: case SKILL_WIN: case PORTRAIT:  case PRESTIGE_SKILLS:
					case LAST_ACTIVE: case GIFT_SKILL:
						this.betResultsRouter.sendDataToQueues((DatabaseResultsData) event);
						break;
					case MATCH_INFO:
						if(currentBetBot != null) {
							currentBetBot.setMatchInfo((MatchInfoEvent) event);
						}
						break;
					case TEAM_INFO:
						TeamInfoEvent teamInfoEvent = (TeamInfoEvent) event;
						if(currentBetBot != null) {
							if(currentBetBot.getLeft() == teamInfoEvent.getTeam()) {
								currentBetBot.addTeamInfo(teamInfoEvent);
							} else if(currentBetBot.getRight() == teamInfoEvent.getTeam()) {
								currentBetBot.addTeamInfo(teamInfoEvent);
							}
						}
						break;
					case UNIT_INFO:
						if(event instanceof UnitInfoEvent && currentBetBot != null) {
							boolean isCommandMadeByBot = currentBetBot.getTeamData().addUnitInfo((UnitInfoEvent) event);
							if(isCommandMadeByBot) {
								if(currentBetBot.getTeamData().nextCommandToRun() != null) {
									this.sendScheduledMessage(currentBetBot.getTeamData().nextCommandToRun(), 5000L);
								}
							}
						}
						break;
					case SKILL_DROP: default:
						break;
				}
				assert(currentAmount != 0);
			} catch (InterruptedException e) {
				log.error("Error found handling event", e);
			}
		}
	}
	
	public void sendScheduledMessage(String message, Long waitTime) {
		this.bettingTimer.schedule(new MessageSenderTask(this.messageSenderRouter, message), waitTime);
	}
	
	public void sendMessage(String message) {
		this.messageSenderRouter.sendDataToQueues(new ChatMessage(message));
	}
	
	private void handleFight(BattleGroundEvent event) {
		return;
	}
	
	public void addToBets(Collection<BetEvent> betEvents, BetEvent newBet) {
		Optional<BetEvent> preExistingEvent = betEvents.stream().filter(betEvent -> StringUtils.equalsIgnoreCase(betEvent.getPlayer(), newBet.getPlayer())).findFirst();
		if(preExistingEvent.isPresent()) {
			betEvents.remove(preExistingEvent.get());
		}
		betEvents.add(newBet);
	}
	
	private void sendBalanceRequest() {
		final String balanceRequest = "!bal";
		this.sendMessage(balanceRequest);
	}

}
