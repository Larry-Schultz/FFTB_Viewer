package fft_battleground.repo;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import fft_battleground.botland.model.BalanceType;
import fft_battleground.botland.model.BalanceUpdateSource;
import fft_battleground.botland.model.BetResults;
import fft_battleground.botland.model.SkillType;
import fft_battleground.event.BattleGroundEventBackPropagation;
import fft_battleground.event.model.AllegianceEvent;
import fft_battleground.event.model.BalanceEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BetEvent;
import fft_battleground.event.model.ExpEvent;
import fft_battleground.event.model.LastActiveEvent;
import fft_battleground.event.model.LevelUpEvent;
import fft_battleground.event.model.OtherPlayerBalanceEvent;
import fft_battleground.event.model.PlayerSkillEvent;
import fft_battleground.event.model.PortraitEvent;
import fft_battleground.event.model.PrestigeSkillsEvent;
import fft_battleground.event.model.TeamInfoEvent;
import fft_battleground.repo.model.BalanceHistory;
import fft_battleground.repo.model.GlobalGilHistory;
import fft_battleground.repo.model.Match;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.util.GambleUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RepoTransactionManager {

	@Autowired
	private PlayerRecordRepo playerRecordRepo;
	
	@Autowired
	private MatchRepo matchRepo;
	
	@Autowired
	private BalanceHistoryRepo balanceHistoryRepo;
	
	@Autowired
	private GlobalGilHistoryRepo globalGilHistoryRepo;
	
	@Autowired
	private BattleGroundEventBackPropagation battleGroundEventBackPropagation;
	
	public RepoTransactionManager() {}
	
	public RepoTransactionManager(PlayerRecordRepo playerRecordRepo, MatchRepo matchRepo, BalanceHistoryRepo balanceHistoryRepo,
			BattleGroundEventBackPropagation battleGroundEventBackPropagation) {
		this.playerRecordRepo = playerRecordRepo;
		this.matchRepo = matchRepo;
		this.balanceHistoryRepo = balanceHistoryRepo;
		this.battleGroundEventBackPropagation = battleGroundEventBackPropagation;
	}
	
	@SneakyThrows
	public void sendBalanceBackpropagation(BetEvent bet, Float teamBettingOdds, boolean win) {
		Integer valueUpdate = GambleUtil.getAmountUpdateFromBet(bet.getBetAmountInteger(), teamBettingOdds, false);
		BattleGroundEvent balanceEvent = this.generateSimulatedBalanceEvent(bet.getPlayer(), valueUpdate, BalanceUpdateSource.BET);
		this.battleGroundEventBackPropagation.SendUnitThroughTimer(balanceEvent);
	}
	
	
	@Transactional
	public void reportAsLoss(List<BetEvent> bets, Float teamBettingOdds) {
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
	public void reportAsFightLoss(TeamInfoEvent team) {
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
	public void reportAsWin(List<BetEvent> bets, Float teamBettingOdds) {
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
	public void reportAsFightWin(TeamInfoEvent team) {
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
	public void updateMatchData(BetResults result) {
		log.info("updating match data.");
		Match mostRecentMatch = new Match(result);
		this.matchRepo.saveAndFlush(mostRecentMatch);
	}
	
	@Transactional
	public void updatePlayerPortrait(PortraitEvent event) {
		Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(StringUtils.lowerCase(event.getPlayer()));
		if(maybeRecord.isPresent()) {
			PlayerRecord record = maybeRecord.get();
			record.setPortrait(event.getPortrait());
			this.playerRecordRepo.saveAndFlush(record);
		} else {
			event.setPlayer(StringUtils.lowerCase(event.getPlayer()));
			PlayerRecord record = new PlayerRecord(event);
			this.playerRecordRepo.saveAndFlush(record);
		}
	}
	
	@Transactional
	public void updatePlayerAmount(BalanceEvent event) {
		Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(StringUtils.lowerCase(event.getPlayer()));
		if(maybeRecord.isPresent()) {
			PlayerRecord record = maybeRecord.get();
			record.setLastKnownAmount(event.getAmount());
			if(record.getHighestKnownAmount() == null || event.getAmount() > record.getHighestKnownAmount()) {
				record.setHighestKnownAmount(event.getAmount());
			}
			this.playerRecordRepo.saveAndFlush(record);
		} else {
			event.setPlayer(StringUtils.lowerCase(event.getPlayer()));
			PlayerRecord record = new PlayerRecord(event);
			this.playerRecordRepo.saveAndFlush(record);
		}
	}
	
	@Transactional
	public void updatePlayerLevel(LevelUpEvent event) {
		String id = StringUtils.lowerCase(event.getPlayer());
		event.setPlayer(id);
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
	public void updatePlayerAllegiance(AllegianceEvent event) {
		String id = StringUtils.lowerCase(event.getPlayer());
		Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(id);
		if(maybeRecord.isPresent()) {
			maybeRecord.get().setAllegiance(event.getTeam());
			this.playerRecordRepo.saveAndFlush(maybeRecord.get());
		} else {
			event.setPlayer(StringUtils.lowerCase(event.getPlayer()));
			PlayerRecord record = new PlayerRecord(event);
			this.playerRecordRepo.saveAndFlush(record);
		}
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public void updatePlayerSkills(PlayerSkillEvent event) {
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
	public void updatePlayerLastActive(LastActiveEvent event) {
		String id = StringUtils.lowerCase(event.getPlayer());
		Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(id);
		if(maybeRecord.isPresent()) {
			maybeRecord.get().setLastActive(event.getLastActive());
			this.playerRecordRepo.saveAndFlush(maybeRecord.get());
		} else {
			event.setPlayer(StringUtils.lowerCase(event.getPlayer()));
			PlayerRecord record = new PlayerRecord(event);
			this.playerRecordRepo.saveAndFlush(record);
		}
	}
	
	@Transactional
	public void updateGlobalGilHistory(GlobalGilHistory globalGilHistory) {
		Optional<GlobalGilHistory> maybeGlobalGilHistory = this.globalGilHistoryRepo.findById(globalGilHistory.getDate_string());
		
		if(maybeGlobalGilHistory.isPresent()) {
			//if present, update
			GlobalGilHistory existingGlobalGilHistory = maybeGlobalGilHistory.get();
			existingGlobalGilHistory.setGlobal_gil_count(globalGilHistory.getGlobal_gil_count());
			existingGlobalGilHistory.setPlayer_count(globalGilHistory.getPlayer_count());
			this.globalGilHistoryRepo.saveAndFlush(existingGlobalGilHistory);
		} else {
			this.globalGilHistoryRepo.saveAndFlush(globalGilHistory);
		}
		
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public BattleGroundEvent generateSimulatedBalanceEvent(String player, int balanceUpdate, BalanceUpdateSource balanceUpdateSource) {
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
			
			event = new OtherPlayerBalanceEvent(player, newSimulatedAmount, BalanceType.SIMULATED, balanceUpdateSource);
			event.getOtherPlayerBalanceEvents().get(0).setBalanceChange(balanceUpdate);
		}
		
		return event;
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public void addEntryToBalanceHistory(BalanceEvent event) {
		String id = StringUtils.lowerCase(event.getPlayer());
		BalanceHistory newBalance = new BalanceHistory(event);
		this.balanceHistoryRepo.saveAndFlush(newBalance);
		
		return;
	}
}
