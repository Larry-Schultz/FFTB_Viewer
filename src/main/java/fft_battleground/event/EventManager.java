package fft_battleground.event;

import java.util.List;
import java.util.Timer;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fft_battleground.botland.BetBotFactory;
import fft_battleground.botland.BotLand;
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
import fft_battleground.model.BattleGroundTeam;
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
	
	@Autowired
	private DumpService dumpService;
	
	private Timer bettingTimer = new Timer();
	protected long bettingDelay = 33 * 1000;
	
	public EventManager() {
		this.setName("EventManagerThread");
	}
	
	private BotLand botLand;
	
	@Override
	public void run() {
		int currentAmount = 1;
		List<BetEvent> currentBets = new Vector<>();
		boolean bettingCurrently = false;
		//BetBot previousBetBot = null;
		//BetBot currentBetBot = null;
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
							//currentBetBot = this.betBotFactory.create(DataBetBot.class, currentAmount, currentBets, beginEvent);
							this.botLand = this.betBotFactory.createBotLand(currentAmount, currentBets, beginEvent);
							this.bettingTimer.schedule(this.botLand, this.bettingDelay);
						}
						break;
					case BETTING_ENDS:
						bettingCurrently = false;
						BettingEndsEvent bettingEndsEvent = (BettingEndsEvent) event;
						if(this.botLand != null) {
							this.botLand.setBettingEndsEvent(bettingEndsEvent);
						}
						if(bettingEndsEvent.getTeam1() == BattleGroundTeam.RED && bettingEndsEvent.getTeam2() == BattleGroundTeam.BLUE) {
							this.bettingTimer.schedule(this.dumpService.getGlobalGilUpdateTask(), 5L);
						}
						break;
					case BALANCE:
						BalanceEvent balanceEvent = (BalanceEvent) event;
						currentAmount = balanceEvent.getAmount();
						if(this.botLand != null) {
							this.botLand.setCurrentAmountToBetWith(currentAmount);
						}
						break;
					case BET:
						if(bettingCurrently && event instanceof BetEvent  && this.botLand != null) {
							this.botLand.addBet((BetEvent) event);
						}
						break;
					case BET_INFO:
						if(event instanceof BetInfoEvent && this.botLand != null) {
							BetInfoEvent betInfoEvent = (BetInfoEvent) event;
							if(betInfoEvent.getTeam() != null) {
								this.botLand.addBetInfo(betInfoEvent);
							}
						}
						break;
					case BAD_BET:
						if(bettingCurrently && event instanceof BadBetEvent && this.botLand != null) {
							this.botLand.removeBet((BadBetEvent)event);
						}
						break;
					case RESULT:
						if(this.botLand != null && event instanceof ResultEvent) {
							BetResults aftermathOfBotBet = this.botLand.createCollector().getResult((ResultEvent) event);
							this.betResultsRouter.sendDataToQueues(aftermathOfBotBet);
						}
						break;
					case OTHER_PLAYER_BALANCE: case LEVEL_UP: case OTHER_PLAYER_EXP: case ALLEGIANCE:
					case PLAYER_SKILL: case BUY_SKILL: case SKILL_WIN: case PORTRAIT:  case PRESTIGE_SKILLS:
					case LAST_ACTIVE: case GIFT_SKILL: case GLOBAL_GIL_COUNT_UPDATE: case PRESTIGE_ASCENSION:
						this.betResultsRouter.sendDataToQueues((DatabaseResultsData) event);
						break;
					case MATCH_INFO:
						if(this.botLand!= null) {
							this.botLand.setMatchInfo((MatchInfoEvent) event);
						}
						break;
					case TEAM_INFO:
						TeamInfoEvent teamInfoEvent = (TeamInfoEvent) event;
						if(this.botLand != null) {
							this.botLand.addTeamInfo(teamInfoEvent);
						}
						break;
					case UNIT_INFO:
						if(event instanceof UnitInfoEvent && this.botLand != null) {
							this.botLand.addUnitInfo((UnitInfoEvent) event);
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
	
	private void sendBalanceRequest() {
		final String balanceRequest = "!bal";
		this.sendMessage(balanceRequest);
	}

}
