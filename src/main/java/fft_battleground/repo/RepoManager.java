package fft_battleground.repo;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import fft_battleground.BattleGroundEventBackPropagation;
import fft_battleground.botland.model.BalanceType;
import fft_battleground.botland.model.BalanceUpdateSource;
import fft_battleground.botland.model.BetResults;
import fft_battleground.botland.model.DatabaseResultsData;
import fft_battleground.botland.model.SkillType;
import fft_battleground.dump.DumpService;
import fft_battleground.event.model.AllegianceEvent;
import fft_battleground.event.model.BalanceEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BetEvent;
import fft_battleground.event.model.BuySkillEvent;
import fft_battleground.event.model.ExpEvent;
import fft_battleground.event.model.GiftSkillEvent;
import fft_battleground.event.model.LastActiveEvent;
import fft_battleground.event.model.LevelUpEvent;
import fft_battleground.event.model.OtherPlayerBalanceEvent;
import fft_battleground.event.model.OtherPlayerExpEvent;
import fft_battleground.event.model.PlayerSkillEvent;
import fft_battleground.event.model.PortraitEvent;
import fft_battleground.event.model.PrestigeSkillsEvent;
import fft_battleground.event.model.SkillWinEvent;
import fft_battleground.event.model.TeamInfoEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.repo.model.BalanceHistory;
import fft_battleground.repo.model.Match;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.util.GambleUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RepoManager extends Thread {

	@Autowired
	private PlayerRecordRepo playerRecordRepo;
	
	@Autowired
	private MatchRepo matchRepo;
	
	@Autowired
	private BalanceHistoryRepo balanceHistoryRepo;
	
	@Autowired
	private BlockingQueue<DatabaseResultsData> betResultsQueue;
	
	@Autowired
	private BattleGroundEventBackPropagation battleGroundEventBackPropagation;
	
	@Autowired
	private DumpService dumpService;
	
	public RepoManager() {
		this.setName("RepoManagerThread");
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				DatabaseResultsData newResults = this.betResultsQueue.take();
				if(newResults instanceof BetResults) {
					this.handleBetResult((BetResults) newResults);
				} else if(newResults instanceof OtherPlayerBalanceEvent) {
					this.handleOtherPlayerBalanceEvent((OtherPlayerBalanceEvent) newResults);
				} else if(newResults instanceof LevelUpEvent) {
					this.handleLevelUpEvent((LevelUpEvent) newResults);
				} else if(newResults instanceof OtherPlayerExpEvent) {
					this.handleOtherPlayerExpEvent((OtherPlayerExpEvent) newResults);
				} else if(newResults instanceof AllegianceEvent) {
					this.handleAllegianceEvent((AllegianceEvent) newResults);
				} else if(newResults instanceof PrestigeSkillsEvent) {
					this.handlePrestigeSkillsEvent((PrestigeSkillsEvent) newResults);
				} else if(newResults instanceof PlayerSkillEvent) {
					this.handlePlayerSkillEvent((PlayerSkillEvent) newResults);
				} else if(newResults instanceof BuySkillEvent) {
					this.handleBuySkillEvent((BuySkillEvent) newResults);
				} else if(newResults instanceof SkillWinEvent) {
					this.handleSkillWinEvent((SkillWinEvent) newResults);
				} else if(newResults instanceof PortraitEvent) {
					this.handlePortraitEvent((PortraitEvent) newResults);
				} else if(newResults instanceof LastActiveEvent) {
					this.handleLastActiveSkillEvent((LastActiveEvent) newResults);
				} else if(newResults instanceof GiftSkillEvent) {
					this.handleGiftSkillEvent((GiftSkillEvent) newResults);
				}
			} catch (InterruptedException e) {
				log.error("error in RepoManager", e);
			}
		}
	}

	protected void handleBetResult(BetResults newResults) {
		log.info("found new bet results: {}", newResults.betResultsInfo());
		log.info("The bot thinks that: {} won.", BattleGroundTeam.getTeamName(newResults.getWinningTeam()));
		log.info("The two teams were: {} and {}", newResults.getLeftTeam(), newResults.getRightTeam());
		
		assert(newResults.getTeamData().getLeftTeamData() != null);
		assert(newResults.getTeamData().getRightTeamData() != null);
		
		//update player data
		this.updatePlayerData(newResults);
		
		//update match data
		this.updateMatchData(newResults);
	}
	
	protected void handlePortraitEvent(PortraitEvent event) {
		this.updatePlayerPortrait(event);
	}
	
	protected void handleSkillWinEvent(SkillWinEvent event) {
		for(PlayerSkillEvent skillEvent: event.getSkillEvents()) {
			this.updatePlayerSkills(skillEvent);
		}
	}
	
	protected void handleOtherPlayerBalanceEvent(OtherPlayerBalanceEvent event) {
		if(event != null && event.getOtherPlayerBalanceEvents() != null) {
			for(BalanceEvent balanceEvent : event.getOtherPlayerBalanceEvents()) {
				this.updatePlayerAmount(balanceEvent);
				this.addEntryToBalanceHistory(balanceEvent);
				this.dumpService.updateBalanceCache(balanceEvent);
			}
		}
	}
	
	protected void handleLevelUpEvent(LevelUpEvent event) {
		this.updatePlayerLevel(event);
	}
	
	protected void handleOtherPlayerExpEvent(OtherPlayerExpEvent event) {
		for(ExpEvent expEvent : event.getExpEvents()) {
			this.updatePlayerLevel((LevelUpEvent) expEvent);
		}
	}
	
	protected void handleAllegianceEvent(AllegianceEvent event) {
		this.updatePlayerAllegiance(event);
	}
	
	protected void handlePrestigeSkillsEvent(PrestigeSkillsEvent event) {
		this.handlePlayerSkillEvent((PlayerSkillEvent) event);
	}
	
	protected void handlePlayerSkillEvent(PlayerSkillEvent event) {
		this.updatePlayerSkills(event);
	}
	
	@SneakyThrows
	protected void handleBuySkillEvent(BuySkillEvent event) {
		for(PlayerSkillEvent playerSkillEvent : event.getSkillEvents()) {
			this.handlePlayerSkillEvent(playerSkillEvent);
			BattleGroundEvent balanceUpdate = this.generateSimulatedBalanceEvent(playerSkillEvent.getPlayer(), BuySkillEvent.skillBuyBalanceUpdate);
			if(balanceUpdate != null) {
				this.battleGroundEventBackPropagation.SendUnitThroughTimer(balanceUpdate);
			}
		}
	}
	
	protected void handleLastActiveSkillEvent(LastActiveEvent event) {
		this.updatePlayerLastActive(event);
	}
	
	private void handleGiftSkillEvent(GiftSkillEvent newResults) {
		this.updatePlayerSkills(newResults.getPlayerSkillEvent());
		this.generateSimulatedBalanceEvent(newResults.getGivingPlayer(), newResults.getCost());
	}
	
	protected void updatePlayerData(BetResults newResults) {
		
		Float leftTeamOdds = GambleUtil.bettingOdds(newResults.getFullBettingData().getTeam1Amount(), newResults.getFullBettingData().getTeam2Amount());
		Float rightTeamOdds = GambleUtil.bettingOdds(newResults.getFullBettingData().getTeam2Amount(), newResults.getFullBettingData().getTeam1Amount());
		//store player records first
		if(newResults.getWinningTeam() == newResults.getLeftTeam()) {
			this.reportAsWin(newResults.getBets().getLeft(), leftTeamOdds);
		} else if(newResults.getWinningTeam() == newResults.getRightTeam()){
			this.reportAsLoss(newResults.getBets().getLeft(), leftTeamOdds);
		}
		
		if(newResults.getWinningTeam() == newResults.getLeftTeam()) {
			this.reportAsLoss(newResults.getBets().getRight(), rightTeamOdds);
		} else if(newResults.getWinningTeam() == newResults.getRightTeam()){
			this.reportAsWin(newResults.getBets().getRight(), rightTeamOdds);
		}
		
		//now update the fight stats for the players
		if(newResults.getWinningTeam() == newResults.getLeftTeam()) {
			this.reportAsFightWin(newResults.getTeamData().getLeftTeamData());
		} else if(newResults.getWinningTeam() == newResults.getRightTeam()){
			this.reportAsFightLoss(newResults.getTeamData().getLeftTeamData());
			
		}
		
		if(newResults.getWinningTeam() == newResults.getLeftTeam()) {
			this.reportAsFightLoss(newResults.getTeamData().getRightTeamData());
		} else if(newResults.getWinningTeam() == newResults.getRightTeam()){
			this.reportAsFightWin(newResults.getTeamData().getRightTeamData());
		}
	}
	
	@Transactional
	protected void reportAsLoss(List<BetEvent> bets, Float teamBettingOdds) {
		for(BetEvent bet : bets) {
			PlayerRecord player = null;
			Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(StringUtils.lowerCase(bet.getPlayer()));
			if(!maybeRecord.isPresent()) {
				player = new PlayerRecord(StringUtils.lowerCase(bet.getPlayer()), 0, 1);
			} else {
				player = maybeRecord.get();
				player.setLosses(player.getLosses() + 1);
			}
			this.playerRecordRepo.saveAndFlush(player);
			
			//now let's backpropagate some simulated balance updates
			sendBalanceBackpropagation(bet, teamBettingOdds, false);
		}
		
	}
	
	@Transactional
	protected void reportAsFightLoss(TeamInfoEvent team) {
		for(Pair<String, String> teamMemberInfo: team.getPlayerUnitPairs()) {
			Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(StringUtils.lowerCase(teamMemberInfo.getLeft()));
			if(maybeRecord.isPresent()) {
				PlayerRecord record = maybeRecord.get();
				Integer currentFightLosses = record.getFightLosses();
				currentFightLosses = currentFightLosses + 1;
				record.setFightLosses(currentFightLosses);
				
				this.playerRecordRepo.saveAndFlush(record);
			}
		}
	}
	
	@Transactional
	protected void reportAsWin(List<BetEvent> bets, Float teamBettingOdds) {
		for(BetEvent bet : bets) {
			PlayerRecord player = null;
			Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(StringUtils.lowerCase(bet.getPlayer()));
			if(!maybeRecord.isPresent()) {
				player = new PlayerRecord(StringUtils.lowerCase(bet.getPlayer()), 1, 0);
			} else {
				player = maybeRecord.get();
				player.setWins(player.getWins() + 1);
			}
			
			this.playerRecordRepo.saveAndFlush(player);
			
			//now let's backpropagate some simulated balance updates
			sendBalanceBackpropagation(bet, teamBettingOdds, true);
		}
		
	}
	
	@Transactional
	protected void reportAsFightWin(TeamInfoEvent team) {
		for(Pair<String, String> teamMemberInfo: team.getPlayerUnitPairs()) {
			Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(StringUtils.lowerCase(teamMemberInfo.getLeft()));
			if(maybeRecord.isPresent()) {
				PlayerRecord record = maybeRecord.get();
				Integer currentFightWins = record.getFightWins();
				currentFightWins = currentFightWins + 1;
				record.setFightWins(currentFightWins);
				
				this.playerRecordRepo.saveAndFlush(record);
			}
		}
	}
	
	@Transactional
	protected void updateMatchData(BetResults result) {
		log.info("updating match data.");
		Match mostRecentMatch = new Match(result);
		this.matchRepo.saveAndFlush(mostRecentMatch);
	}
	
	@Transactional
	protected void updatePlayerPortrait(PortraitEvent event) {
		Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(StringUtils.lowerCase(event.getPlayer()));
		if(maybeRecord.isPresent()) {
			PlayerRecord record = maybeRecord.get();
			record.setPortrait(event.getPortrait());
			this.playerRecordRepo.saveAndFlush(record);
		} else {
			PlayerRecord record = new PlayerRecord(event);
			this.playerRecordRepo.saveAndFlush(record);
		}
	}
	
	@Transactional
	protected void updatePlayerAmount(BalanceEvent event) {
		Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(StringUtils.lowerCase(event.getPlayer()));
		if(maybeRecord.isPresent()) {
			PlayerRecord record = maybeRecord.get();
			record.setLastKnownAmount(event.getAmount());
			if(record.getHighestKnownAmount() == null || event.getAmount() > record.getHighestKnownAmount()) {
				record.setHighestKnownAmount(event.getAmount());
			}
			this.playerRecordRepo.saveAndFlush(record);
		} else {
			PlayerRecord record = new PlayerRecord(event);
			this.playerRecordRepo.saveAndFlush(record);
		}
	}
	
	@Transactional
	protected void updatePlayerLevel(LevelUpEvent event) {
		String id = StringUtils.lowerCase(event.getPlayer());
		Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(id);
		if(maybeRecord.isPresent()) {
			maybeRecord.get().setLastKnownLevel(event.getLevel());
			if(event instanceof ExpEvent) {
				Short remainingExp = ((ExpEvent)event).getRemainingExp();
				maybeRecord.get().setLastKnownRemainingExp(remainingExp);
			} else {
				maybeRecord.get().setLastKnownRemainingExp(GambleUtil.DEFAULT_REMAINING_EXP);
			}
			this.playerRecordRepo.saveAndFlush(maybeRecord.get());
		} else {
			PlayerRecord record = null;
			if(event instanceof ExpEvent) {
				ExpEvent expEvent = (ExpEvent) event;
				record = new PlayerRecord(expEvent);
			} else {
				record = new PlayerRecord(event);
			}
			
			this.playerRecordRepo.saveAndFlush(record);
		}
	}
	
	@Transactional
	protected void updatePlayerAllegiance(AllegianceEvent event) {
		String id = StringUtils.lowerCase(event.getPlayer());
		Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(id);
		if(maybeRecord.isPresent()) {
			maybeRecord.get().setAllegiance(event.getTeam());
			this.playerRecordRepo.saveAndFlush(maybeRecord.get());
		} else {
			PlayerRecord record = new PlayerRecord(event);
			this.playerRecordRepo.saveAndFlush(record);
		}
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	protected void updatePlayerSkills(PlayerSkillEvent event) {
		String id = StringUtils.lowerCase(event.getPlayer());
		Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(id);
		if(maybeRecord.isPresent()) {
			Hibernate.initialize(maybeRecord.get().getPlayerSkills());
			List<String> currentSkills = maybeRecord.get().getPlayerSkills().stream().map(playerSkill -> playerSkill.getSkill()).collect(Collectors.toList());
			for(String possibleNewSkill: event.getSkills()) {
				if(!currentSkills.contains(possibleNewSkill)) {
					if(event instanceof PrestigeSkillsEvent) {
						maybeRecord.get().addPlayerSkill(possibleNewSkill, SkillType.PRESTIGE);
					} else {
						maybeRecord.get().addPlayerSkill(possibleNewSkill, SkillType.USER);
					}
				}
			}
			this.playerRecordRepo.saveAndFlush(maybeRecord.get());
		}
	}
	
	@Transactional
	protected void updatePlayerLastActive(LastActiveEvent event) {
		String id = StringUtils.lowerCase(event.getPlayer());
		Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(id);
		if(maybeRecord.isPresent()) {
			maybeRecord.get().setLastActive(event.getLastActive());
			this.playerRecordRepo.saveAndFlush(maybeRecord.get());
		} else {
			PlayerRecord record = new PlayerRecord(event);
			this.playerRecordRepo.saveAndFlush(record);
		}
	}
	
	@SneakyThrows
	public void sendBalanceBackpropagation(BetEvent bet, Float teamBettingOdds, boolean win) {
		Integer valueUpdate = GambleUtil.getAmountUpdateFromBet(bet.getBetAmountInteger(), teamBettingOdds, false);
		BattleGroundEvent balanceEvent = this.generateSimulatedBalanceEvent(bet.getPlayer(), valueUpdate);
		this.battleGroundEventBackPropagation.SendUnitThroughTimer(balanceEvent);
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	protected BattleGroundEvent generateSimulatedBalanceEvent(String player, int balanceUpdate) {
		OtherPlayerBalanceEvent event = null;
		
		String id = StringUtils.lowerCase(player);
		Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(id);
		if(maybeRecord.isPresent()) {
			Integer currentKnownBalance = maybeRecord.get().getLastKnownAmount();
			if(currentKnownBalance == null) {
				currentKnownBalance = GambleUtil.MINIMUM_BET;
			}
			Integer newSimulatedAmount = currentKnownBalance + balanceUpdate;
			
			//if balance goes below the minimum for player, then return the minimum
			Integer minimumBalanceForPlayer = GambleUtil.getMinimumBetForLevel(maybeRecord.get().getLastKnownLevel());
			if(newSimulatedAmount < minimumBalanceForPlayer) {
				newSimulatedAmount = minimumBalanceForPlayer;
			}
			
			event = new OtherPlayerBalanceEvent(player, newSimulatedAmount, BalanceType.SIMULATED, BalanceUpdateSource.BET);
		}
		
		return event;
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	protected void addEntryToBalanceHistory(BalanceEvent event) {
		String id = StringUtils.lowerCase(event.getPlayer());
		BalanceHistory newBalance = new BalanceHistory(event);
		this.balanceHistoryRepo.saveAndFlush(newBalance);
		
		return;
	}
}
