package fft_battleground.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fft_battleground.BattleGroundEventBackPropagation;
import fft_battleground.dump.DumpService;
import fft_battleground.event.detector.EventDetector;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BetEvent;
import fft_battleground.event.model.BetInfoEvent;
import fft_battleground.event.model.BettingBeginsEvent;
import fft_battleground.event.model.FightBeginsEvent;
import fft_battleground.event.model.MatchInfoEvent;
import fft_battleground.event.model.SkillDropEvent;
import fft_battleground.event.model.TeamInfoEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.model.ChatMessage;
import fft_battleground.repo.PlayerRecordRepo;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.tournament.Tournament;
import fft_battleground.tournament.TournamentService;
import fft_battleground.util.GambleUtil;
import fft_battleground.util.Router;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EventParser extends Thread {
	
	@Autowired
	private BlockingQueue<ChatMessage> eventParserMessageQueue;
	
	@Autowired
	private Router<BattleGroundEvent> eventRouter;
	
	@Autowired
	private Router<ChatMessage> messageSenderRouter;
	
	@Autowired
	private List<EventDetector> detectors;
	
	@Autowired
	private PlayerRecordRepo playerRecordRepo;
	
	@Autowired
	private TournamentService tournamentService;
	
	@Autowired
	private DumpService dumpService;
	
	@Autowired
	private BattleGroundEventBackPropagation battleGroundEventBackPropagation;
	
	private Timer eventTimer = new Timer();
	
	public EventParser() {
		this.setName("EvntParsrThrd");
	}
	
	private Tournament currentTournament = null;
	private MatchInfoEvent previousMatchEvent = null;
	
	@Override
	@SneakyThrows
	public void run() {
		log.info("Starting parser");
		while(true) {
			try {
				ChatMessage message = eventParserMessageQueue.take();
				log.debug("{}", message);
				for(EventDetector detector : this.detectors) {
					BattleGroundEvent event = detector.detect(message);
					if(event != null) {
						if(!(event instanceof BetEvent)) {
							log.info("Found event: {} with data: {} from chatMessage {}", event.getEventType().getEventStringName(), event.toString(), message);
						} 
						
						if(event instanceof BetEvent) {
							this.attachMetadataToBetEvent((BetEvent) event);
							this.eventRouter.sendDataToQueues(event);
						} else if(event instanceof FightBeginsEvent) {
							FightBeginsEvent fightEvent = (FightBeginsEvent) event;
							SkillDropEvent skillDropEvent = fightEvent.generateSkillDropEvent();
							this.eventRouter.sendDataToQueues(fightEvent);
							this.eventRouter.sendDataToQueues(skillDropEvent);
						} else if(event instanceof BettingBeginsEvent) {
						
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
						} else {
							this.eventRouter.sendDataToQueues(event);
						}
					}
				}
			} catch (InterruptedException e) {
				log.error("Error found in Event Parser", e);
			}
			
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
					this.attachMetadataToTeamInfo(teamInfoEvent);
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
	
	protected void attachMetadataToBetInfoEvent(BetInfoEvent event) {
		PlayerRecord metadata = new PlayerRecord();
		Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(StringUtils.lowerCase(event.getPlayer()));
		if(maybeRecord.isPresent()) {
			PlayerRecord record = maybeRecord.get();
			metadata.setPlayer(event.getPlayer());
			metadata.setWins(record.getWins());
			metadata.setLosses(record.getLosses());
			metadata.setLastKnownAmount(record.getLastKnownAmount());
			event.setMetadata(metadata);
		}
		
		return;
	}
	
	protected void attachMetadataToBetEvent(BetEvent event) {
		PlayerRecord metadata = new PlayerRecord();
		Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(StringUtils.lowerCase(event.getPlayer()));
		if(maybeRecord.isPresent()) {
			PlayerRecord record = maybeRecord.get();
			metadata.setPlayer(event.getPlayer());
			metadata.setWins(record.getWins());
			metadata.setLosses(record.getLosses());
			metadata.setLastKnownAmount(record.getLastKnownAmount());
			event.setMetadata(metadata);
			event.setBetAmount(GambleUtil.getBetAmountFromBetString(record, event).toString());
		} else {
			event.setBetAmount(GambleUtil.MINIMUM_BET.toString());
		}
		
		return;
	}
	
	protected void attachMetadataToTeamInfo(TeamInfoEvent event) {
		List<PlayerRecord> metadataRecords = new ArrayList<>();
		for(Pair<String, String> playerUnitData : event.getPlayerUnitPairs()) {
			PlayerRecord metadata = new PlayerRecord();
			Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(StringUtils.lowerCase(playerUnitData.getLeft()));
			if(maybeRecord.isPresent()) {
				PlayerRecord record = maybeRecord.get();
				metadata.setPlayer(playerUnitData.getLeft());
				metadata.setFightWins(record.getFightWins());
				metadata.setFightLosses(record.getFightLosses());
			} else {
				metadata.setPlayer(playerUnitData.getLeft());
				metadata.setFightWins(0);
				metadata.setFightLosses(0);
			}
			metadataRecords.add(metadata);
		}
		
		event.setMetaData(metadataRecords);
	}
	
	protected void sendScheduledMessage(String message, Long waitTime) {
		this.eventTimer.schedule(new MessageSenderTask(this.messageSenderRouter, message), waitTime);
	}
	
	protected void startEventUpdate() {
		this.eventTimer.schedule(this.dumpService.getDataUpdateTask(), BattleGroundEventBackPropagation.delayIncrement);
	}
	
	/*
	 * need to come up with logic to determine the current teams that are playing purely from the tournament api
	protected void startUpInformationPull() {
		this.currentTournament = this.tournamentService.getcurrentTournament();
		
		List<BattleGroundEvent> tournamentRelatedEvents = currentTournament.getEventsFromTournament(bettingBeginsEvent.getTeam1(), bettingBeginsEvent.getTeam2());
		this.handleTournamentEvents(tournamentRelatedEvents);
	}
	*/
}