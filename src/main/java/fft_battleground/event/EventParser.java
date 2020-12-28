package fft_battleground.event;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.DumpService;
import fft_battleground.event.annotate.BattleGroundEventAnnotator;
import fft_battleground.event.detector.EventDetector;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BetEvent;
import fft_battleground.event.model.BetInfoEvent;
import fft_battleground.event.model.BettingBeginsEvent;
import fft_battleground.event.model.FightBeginsEvent;
import fft_battleground.event.model.MatchInfoEvent;
import fft_battleground.event.model.PrestigeAscensionEvent;
import fft_battleground.event.model.SkillDropEvent;
import fft_battleground.event.model.TeamInfoEvent;
import fft_battleground.event.model.UnitInfoEvent;
import fft_battleground.exception.DumpException;
import fft_battleground.exception.TournamentApiException;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.model.ChatMessage;
import fft_battleground.tournament.TournamentService;
import fft_battleground.tournament.model.Tournament;
import fft_battleground.util.Router;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EventParser extends Thread {
	
	private boolean logEvents = true;
	
	@Autowired
	private BlockingQueue<ChatMessage> eventParserMessageQueue;
	
	@Autowired
	private Router<BattleGroundEvent> eventRouter;
	
	@Autowired
	private Router<ChatMessage> messageSenderRouter;
	
	@Autowired
	private List<EventDetector> detectors;
	
	@Autowired
	private TournamentService tournamentService;
	
	@Autowired
	private DumpService dumpService;
	
	@Autowired
	private BattleGroundEventBackPropagation battleGroundEventBackPropagation;
	
	@Autowired
	private WebhookManager errorWebhookManager;
	
	@Autowired
	private BattleGroundEventAnnotator<BetEvent> betEventAnnotator;
	
	@Autowired
	private BattleGroundEventAnnotator<BetInfoEvent> betInfoEventAnnotator;
	
	@Autowired
	private BattleGroundEventAnnotator<PrestigeAscensionEvent> prestigeAscensionEventAnnotator;
	
	@Autowired
	private BattleGroundEventAnnotator<SkillDropEvent> skillDropEventAnnotator;
	
	@Autowired
	private BattleGroundEventAnnotator<TeamInfoEvent> teamInfoEventAnnotator;
	
	@Autowired
	private BattleGroundEventAnnotator<UnitInfoEvent> unitInfoEventAnnotator;
	
	private Timer eventTimer = new Timer();
	
	public EventParser() {
		this.setName("EvntParsrThrd");
	}
	
	private Tournament currentTournament = null;
	private MatchInfoEvent previousMatchEvent = null;
	
	@Override
	@SneakyThrows
	public void run() {
		this.handleRouterData();
	}
	
	protected void handleRouterData() {
		log.info("Starting parser");
		while(true) {
			ChatMessage message;
			try {
				message = eventParserMessageQueue.take();
				log.debug("{}", message);
				//get events from detectors
				List<BattleGroundEvent> events = this.getEventsFromChatMessage(message);
				for(BattleGroundEvent event : events) {
					//log and handle each event
					this.logEvent(event, message);
					this.handleBattleGroundEvent(event);
				}
			} catch (InterruptedException e) {
				log.warn("Interruption exception in EventParser", e);
			}
			
				
		}
	}
	
	protected List<BattleGroundEvent> getEventsFromChatMessage(ChatMessage message) {
		List<BattleGroundEvent> events = new LinkedList<>();
		for(EventDetector detector : this.detectors) {
			BattleGroundEvent event = detector.detect(message);
			if(event != null) {
				events.add(event);
			}
		}
		
		return events;
	}
	
	protected void handleBattleGroundEvent(BattleGroundEvent event) {
		try {
			switch(event.getEventType()) {
			case BET:
				this.betEventAnnotator.annotateEvent((BetEvent)event);
				this.eventRouter.sendDataToQueues(event);
				break;
			case BET_INFO:
				this.betInfoEventAnnotator.annotateEvent((BetInfoEvent) event);
				this.eventRouter.sendDataToQueues(event);
				break;
			case FIGHT_BEGINS:
				FightBeginsEvent fightEvent = (FightBeginsEvent) event;
				SkillDropEvent skillDropEvent = fightEvent.generateSkillDropEvent();
				this.eventRouter.sendDataToQueues(fightEvent);
				this.skillDropEventAnnotator.annotateEvent(skillDropEvent);
				this.eventRouter.sendDataToQueues(skillDropEvent);
				break;
			case PRESTIGE_ASCENSION:
				this.prestigeAscensionEventAnnotator.annotateEvent((PrestigeAscensionEvent) event);
				this.eventRouter.sendDataToQueues(event);
				break;
			case SKILL_DROP:
				this.skillDropEventAnnotator.annotateEvent((SkillDropEvent) event);
				this.eventRouter.sendDataToQueues(event);
				break;
			case BETTING_ENDS:
				this.handleBettingBeginsEvent(event);
				break;
			case BETTING_BEGINS:
				this.handleBettingEndsEvent(event);
				break;
			default:
				this.eventRouter.sendDataToQueues(event);
				break;
			}
		} catch(DumpException e) {
			log.error("Error found in Event Parser", e);
			this.errorWebhookManager.sendShutdownNotice(e, "Critical error in Event Parser thread");
		} catch(TournamentApiException e) {
			log.warn("TournamentApiException found in Event Parser", e);
		} catch(Exception e) {
			log.error("Error found in Event Parser", e);
			this.errorWebhookManager.sendShutdownNotice(e, "Critical error in Event Parser thread");
		}
	}
	
	protected void logEvent(BattleGroundEvent event, ChatMessage message) {
		if(this.logEvents) {
			log.info("Found event: {} with data: {} from chatMessage {}", event.getEventType().getEventStringName(), event.toString(), message);
		}
	}
	
	protected void handleBettingBeginsEvent(BattleGroundEvent event) throws DumpException {
		this.eventRouter.sendDataToQueues(event);
		if(this.currentTournament != null) {
			List<BattleGroundEvent> finalBets = this.tournamentService.getRealBetInfoFromLatestPotFile(this.currentTournament.getID());
			log.info("Sending final bet data with {} entries", finalBets.size());
			log.info("The final bet event data: {}", finalBets);
			finalBets.parallelStream().forEach(betInfoEvent -> this.betInfoEventAnnotator.annotateEvent((BetInfoEvent) betInfoEvent));
			this.eventRouter.sendAllDataToQueues(finalBets);
		}
	}
	
	protected void handleBettingEndsEvent(BattleGroundEvent event) throws DumpException, TournamentApiException {
		BettingBeginsEvent bettingBeginsEvent = (BettingBeginsEvent) event;
		this.eventRouter.sendDataToQueues(bettingBeginsEvent);
		if( (bettingBeginsEvent.getTeam1() == BattleGroundTeam.RED && bettingBeginsEvent.getTeam2() == BattleGroundTeam.BLUE)
			|| (bettingBeginsEvent.getTeam2() == BattleGroundTeam.RED && bettingBeginsEvent.getTeam1() == BattleGroundTeam.BLUE)
			|| (currentTournament == null) 
			|| (currentTournament.getWinnersCount() >= 7) ) {
			this.currentTournament = this.tournamentService.getcurrentTournament();
			this.startEventUpdate();
		} else {
			if(currentTournament.getWinnersCount() < 7) {
				this.currentTournament.setWinnersCount(currentTournament.getWinnersCount() + 1);
			}
		}
		if(this.currentTournament == null) {
			this.currentTournament = this.tournamentService.getcurrentTournament();
		}
		if(this.currentTournament != null) {
			List<BattleGroundEvent> tournamentRelatedEvents = this.currentTournament.getEventsFromTournament(bettingBeginsEvent.getTeam1(), bettingBeginsEvent.getTeam2());
			this.handleTournamentEvents(tournamentRelatedEvents);
		} else {
			log.error("Contacting the tournament Service has failed!");
		}
	}
	
	@SneakyThrows
	protected void handleTournamentEvents(List<BattleGroundEvent> tournamentRelatedEvents) {
		boolean matchInfoFound = false;
		for(long i = 0; i < tournamentRelatedEvents.size(); i++) {
			BattleGroundEvent battleGroundEvent = tournamentRelatedEvents.get((int) i);
			if(battleGroundEvent != null) {
				switch(battleGroundEvent.getEventType()) {
				case TEAM_INFO:
					TeamInfoEvent teamInfoEvent = (TeamInfoEvent) battleGroundEvent;
					this.teamInfoEventAnnotator.annotateEvent(teamInfoEvent);
					break;
				case MATCH_INFO:
					MatchInfoEvent matchEvent = (MatchInfoEvent) battleGroundEvent;
					if(matchEvent != null && matchEvent.getMapName() != null && matchEvent.getMapNumber() != null) {
						if(previousMatchEvent == null) {
							this.previousMatchEvent = matchEvent;
							matchInfoFound = true;
						} else if(StringUtils.equals(matchEvent.getMapName(), this.previousMatchEvent.getMapName())) {
							matchInfoFound = false;
						} else {
							this.previousMatchEvent = matchEvent;
							matchInfoFound = true;
						}
					}
					break;
				case UNIT_INFO:
					UnitInfoEvent unitEvent = (UnitInfoEvent) battleGroundEvent;
					this.unitInfoEventAnnotator.annotateEvent(unitEvent);
					break;
				default:
					break;
					
				}
				
				this.battleGroundEventBackPropagation.sendUnitThroughTimer(battleGroundEvent, i+1);
			}
		}
		
		if(!matchInfoFound) {
			this.sendScheduledMessage("!match ", 5*1000L);
		}
	}
	
	protected void sendScheduledMessage(String message, Long waitTime) {
		this.eventTimer.schedule(new MessageSenderTask(this.messageSenderRouter, message), waitTime);
	}
	
	protected void startEventUpdate() {
		this.eventTimer.schedule(this.dumpService.getDataUpdateTask(), BattleGroundEventBackPropagation.delayIncrement);
	}
}