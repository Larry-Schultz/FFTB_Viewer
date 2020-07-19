package fft_battleground.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fft_battleground.dump.DumpService;
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
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.model.ChatMessage;
import fft_battleground.repo.PlayerRecordRepo;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.tournament.Tips;
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
						if(true) {
							log.info("Found event: {} with data: {} from chatMessage {}", event.getEventType().getEventStringName(), event.toString(), message);
						} 
						
						if(event instanceof BetEvent) {
							this.attachMetadataToBetEvent((BetEvent) event);
							this.eventRouter.sendDataToQueues(event);
						} else if(event instanceof BetInfoEvent) {
							this.attachMetadataToBetInfoEvent((BetInfoEvent) event);
							this.eventRouter.sendDataToQueues(event);
						} else if(event instanceof FightBeginsEvent) {
							FightBeginsEvent fightEvent = (FightBeginsEvent) event;
							SkillDropEvent skillDropEvent = fightEvent.generateSkillDropEvent();
							this.eventRouter.sendDataToQueues(fightEvent);
							this.attachMetadataToSkillDropEvent(skillDropEvent);
							this.eventRouter.sendDataToQueues(skillDropEvent);
						} else if(event instanceof PrestigeAscensionEvent) {
							this.attachMetadataToPrestigeAscension((PrestigeAscensionEvent) event);
							this.eventRouter.sendDataToQueues(event);
						} else if(event instanceof SkillDropEvent) {
							this.attachMetadataToSkillDropEvent((SkillDropEvent) event);
							this.eventRouter.sendDataToQueues(event);
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
				case UNIT_INFO:
					UnitInfoEvent unitEvent = (UnitInfoEvent) battleGroundEvent;
					this.attachMetadataToUnitInfo(unitEvent);
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
			/*
			 * we set the gold source value for bet info in Botland.addBetInfo, but we get the current data from the database in case its set.
			 * that way the live page has improved bet data for BetInfo bets
			 */
			event.setIsSubscriber(record.isSubscriber());
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
		List<Pair<String, String>> replacementPairList = new ArrayList<>();
		for(Pair<String, String> playerUnitData : event.getPlayerUnitPairs()) {
			PlayerRecord metadata = new PlayerRecord();
			
			//because names from the tournament api have '_' replaced with ' '.  multiple '_' are replaced with a single ' ' 
			String playerName = playerUnitData.getLeft();
			playerName = StringUtils.lowerCase(playerName);
			String likePlayerNameString = StringUtils.replace(playerName, " ", "%"); 
			List<PlayerRecord> records = this.playerRecordRepo.findLikePlayer(likePlayerNameString);
			
			if(records != null && records.size() > 0) {
				PlayerRecord record = records.get(0);
				metadata.setPlayer(record.getPlayer());
				metadata.setFightWins(record.getFightWins());
				metadata.setFightLosses(record.getFightLosses());
				Pair<String, String> newPair = new ImmutablePair<>(record.getPlayer(), playerUnitData.getRight());
				replacementPairList.add(newPair);
			} else {
				metadata.setPlayer(playerUnitData.getLeft());
				metadata.setFightWins(0);
				metadata.setFightLosses(0);
				replacementPairList.add(playerUnitData);
			}
			metadataRecords.add(metadata);
		}
		event.setPlayerUnitPairs(replacementPairList);
		
		event.setMetaData(metadataRecords);
	}
	
	protected void attachMetadataToUnitInfo(UnitInfoEvent event) {
		String playerName = event.getPlayer();
		playerName = StringUtils.lowerCase(playerName);
		String likePlayerNameString = StringUtils.replace(playerName, " ", "%"); 
		List<String> possiblePlayerNames = this.playerRecordRepo.findPlayerNameByLike(likePlayerNameString);
		if(possiblePlayerNames != null && possiblePlayerNames.size() > 0) {
			event.setPlayer(possiblePlayerNames.get(0));
		} else {
			event.setPlayer(playerName);
		}
	}
	
	protected void attachMetadataToPrestigeAscension(PrestigeAscensionEvent event) {
		Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(StringUtils.lowerCase(event.getPrestigeSkillsEvent().getPlayer()));
		if(maybeRecord.isPresent()) {
			event.setCurrentBalance(maybeRecord.get().getLastKnownAmount());
		}
		
		return;
	}
	
	protected void attachMetadataToSkillDropEvent(SkillDropEvent event) {
		if(event != null) {
			Tips tipFromTournamentService = this.tournamentService.getCurrentTips();
			String description = tipFromTournamentService.getUserSkill().get(event.getSkill());
			description = StringUtils.replace(description, "\"", "");
			event.setSkillDescription(description);
			return;
		}
	}
	
	protected void sendScheduledMessage(String message, Long waitTime) {
		this.eventTimer.schedule(new MessageSenderTask(this.messageSenderRouter, message), waitTime);
	}
	
	protected void startEventUpdate() {
		this.eventTimer.schedule(this.dumpService.getDataUpdateTask(), BattleGroundEventBackPropagation.delayIncrement);
	}
}