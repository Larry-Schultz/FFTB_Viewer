package fft_battleground.repo;

import java.util.ArrayList;
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

import fft_battleground.botland.BetterBetBot;
import fft_battleground.botland.model.BalanceType;
import fft_battleground.botland.model.BalanceUpdateSource;
import fft_battleground.botland.model.BetResults;
import fft_battleground.dump.DumpService;
import fft_battleground.event.BattleGroundEventBackPropagation;
import fft_battleground.event.detector.model.AllegianceEvent;
import fft_battleground.event.detector.model.BalanceEvent;
import fft_battleground.event.detector.model.BattleGroundEvent;
import fft_battleground.event.detector.model.BetEvent;
import fft_battleground.event.detector.model.ExpEvent;
import fft_battleground.event.detector.model.FightEntryEvent;
import fft_battleground.event.detector.model.LastActiveEvent;
import fft_battleground.event.detector.model.LevelUpEvent;
import fft_battleground.event.detector.model.OtherPlayerBalanceEvent;
import fft_battleground.event.detector.model.PlayerSkillEvent;
import fft_battleground.event.detector.model.PortraitEvent;
import fft_battleground.event.detector.model.PrestigeSkillsEvent;
import fft_battleground.event.detector.model.SnubEvent;
import fft_battleground.event.detector.model.TeamInfoEvent;
import fft_battleground.event.detector.model.fake.ClassBonusEvent;
import fft_battleground.event.detector.model.fake.SkillBonusEvent;
import fft_battleground.exception.IncorrectTypeException;
import fft_battleground.repo.model.BalanceHistory;
import fft_battleground.repo.model.BotHourlyData;
import fft_battleground.repo.model.Bots;
import fft_battleground.repo.model.GlobalGilHistory;
import fft_battleground.repo.model.Match;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.repo.model.PlayerSkills;
import fft_battleground.repo.repository.BalanceHistoryRepo;
import fft_battleground.repo.repository.BotsHourlyDataRepo;
import fft_battleground.repo.repository.BotsRepo;
import fft_battleground.repo.repository.GlobalGilHistoryRepo;
import fft_battleground.repo.repository.MatchRepo;
import fft_battleground.repo.repository.PlayerRecordRepo;
import fft_battleground.repo.repository.PlayerSkillRepo;
import fft_battleground.repo.util.SkillType;
import fft_battleground.repo.util.UpdateSource;
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
	private PlayerSkillRepo playerSkillRepo;
	
	@Autowired
	private BotsRepo botsRepo;
	
	@Autowired
	private BotsHourlyDataRepo botsHourlyDataRepo;
	
	@Autowired
	private BattleGroundEventBackPropagation battleGroundEventBackPropagation;
	
	@Autowired
	private DumpService dumpService;
	
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
			Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(this.cleanString(bet.getPlayer()));
			if(!maybeRecord.isPresent()) {
				player = new PlayerRecord(this.cleanString(bet.getPlayer()), 0, 1, bet.getIsSubscriber(), UpdateSource.REPORT_AS_LOSS);
			} else {
				player = maybeRecord.get();
				player.setLosses(player.getLosses() + 1);
				player.setUpdateSource(UpdateSource.REPORT_AS_LOSS);
				player.setIsSubscriber(bet.getIsSubscriber());
			}
			this.playerRecordRepo.saveAndFlush(player);
			
			//now let's backpropagate some simulated balance updates
			sendBalanceBackpropagation(bet, teamBettingOdds, false);
		}
		
	}
	
	@Transactional
	public void reportAsFightLoss(TeamInfoEvent team) {
		for(Pair<String, String> teamMemberInfo: team.getPlayerUnitPairs()) {
			Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(this.cleanString(teamMemberInfo.getLeft()));
			if(maybeRecord.isPresent()) {
				PlayerRecord record = maybeRecord.get();
				Integer currentFightLosses = record.getFightLosses();
				currentFightLosses = currentFightLosses + 1;
				record.setFightLosses(currentFightLosses);
				record.setUpdateSource(UpdateSource.REPORT_AS_FIGHT_LOSS);
				
				this.playerRecordRepo.saveAndFlush(record);
			} else {
				PlayerRecord record = new PlayerRecord(this.cleanString(teamMemberInfo.getLeft()), UpdateSource.REPORT_AS_FIGHT_LOSS);
				record.setFightLosses(1);
				record.setFightWins(0);
				this.playerRecordRepo.saveAndFlush(record);
			}
		}
	}
	
	@Transactional
	public void reportAsWin(List<BetEvent> bets, Float teamBettingOdds) {
		for(BetEvent bet : bets) {
			PlayerRecord player = null;
			Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(this.cleanString(bet.getPlayer()));
			if(!maybeRecord.isPresent()) {
				player = new PlayerRecord(this.cleanString(bet.getPlayer()), 1, 0, bet.getIsSubscriber(), UpdateSource.REPORT_AS_WIN);
			} else {
				player = maybeRecord.get();
				player.setWins(player.getWins() + 1);
				player.setUpdateSource(UpdateSource.REPORT_AS_WIN);
				player.setIsSubscriber(bet.getIsSubscriber());
			}
			
			this.playerRecordRepo.saveAndFlush(player);
			
			//now let's backpropagate some simulated balance updates
			sendBalanceBackpropagation(bet, teamBettingOdds, true);
		}
		
	}
	
	@Transactional
	public void reportAsFightWin(TeamInfoEvent team) {
		for(Pair<String, String> teamMemberInfo: team.getPlayerUnitPairs()) {
			Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(this.cleanString(teamMemberInfo.getLeft()));
			if(maybeRecord.isPresent()) {
				PlayerRecord record = maybeRecord.get();
				Integer currentFightWins = record.getFightWins();
				currentFightWins = currentFightWins + 1;
				record.setFightWins(currentFightWins);
				record.setUpdateSource(UpdateSource.REPORT_AS_FIGHT_WIN);
				
				this.playerRecordRepo.saveAndFlush(record);
			} else {
				PlayerRecord record = new PlayerRecord(this.cleanString(teamMemberInfo.getLeft()), UpdateSource.REPORT_AS_FIGHT_WIN);
				record.setFightWins(1);
				record.setFightLosses(0);
				this.playerRecordRepo.saveAndFlush(record);
			}
		}
	}
	
	@Transactional
	public void reportBotWin(BetterBetBot bot, Float winningOdds) {
		Bots botData = this.botsRepo.getBotByDateStringAndName(bot.getDateFormat(), this.cleanString(bot.getName()));
		if(botData != null) {
			Integer wonGil = GambleUtil.getAmountUpdateFromBet(bot.getResult().getBetAmount(botData.getBalance()), winningOdds, true);
			Integer newBalance = botData.getBalance() + wonGil;
			botData.setBalance(newBalance);
			if(newBalance > botData.getHighestKnownValue()) {
				botData.setHighestKnownValue(newBalance);
			}
			
			Short newWins = (short) (botData.getWins() + 1);
			botData.setWins(newWins);
			
			this.botsRepo.saveAndFlush(botData);
			this.addBotHourlyData(bot, newBalance);
			
			log.info("updated bot {} with new balance {} with state {}", bot.getName(), botData.getBalance(), "win");
		}
	}
	
	@Transactional
	public void reportBotLoss(BetterBetBot bot) {
		Bots botData = this.botsRepo.getBotByDateStringAndName(bot.getDateFormat(), this.cleanString(bot.getName()));
		if(botData != null) {
			Integer newBalance = botData.getBalance() - bot.getResult().getBetAmount(botData.getBalance());
			if(newBalance < GambleUtil.getMinimumBetForBettor(bot.isBotSubscriber())) {
				newBalance = GambleUtil.getMinimumBetForBettor(bot.isBotSubscriber());
			}
			botData.setBalance(newBalance);
			
			Short newWins = (short) (botData.getLosses() + 1);
			botData.setLosses(newWins);
			
			this.botsRepo.saveAndFlush(botData);
			this.addBotHourlyData(bot, newBalance);
			
			log.info("updated bot {} with new balance {} with state {}", bot.getName(), botData.getBalance(), "loss");
		}
	}
	
	@Transactional
	public void addBotHourlyData(BetterBetBot bot, Integer balance) {
		BotHourlyData currentData = this.botsHourlyDataRepo.getBotHourlyDataForBotAndCurrentTime(bot.getName(), BotHourlyData.getHourValueForCurrentTime());
		if(currentData != null) {
			currentData.setBalance(balance);
		} else {
			currentData = new BotHourlyData(bot.getName(), balance);
		}
		
		this.botsHourlyDataRepo.saveAndFlush(currentData);
	}
	
	@Transactional
	public void updateMatchData(BetResults result) {
		log.info("updating match data.");
		Match mostRecentMatch = new Match(result);
		this.matchRepo.saveAndFlush(mostRecentMatch);
	}
	
	@Transactional
	public void updatePlayerPortrait(PortraitEvent event) throws IncorrectTypeException {
		if(StringUtils.isNumeric(event.getPortrait())) {
			throw new IncorrectTypeException("The portrait " + event.getPortrait() + " for player " + event.getPlayer() + " is actually a number");
		}
		Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(this.cleanString(event.getPlayer()));
		if(maybeRecord.isPresent()) {
			PlayerRecord record = maybeRecord.get();
			record.setPortrait(event.getPortrait());
			record.setUpdateSource(UpdateSource.PORTRAIT);
			this.playerRecordRepo.saveAndFlush(record);
		} else {
			event.setPlayer(this.cleanString(event.getPlayer()));
			PlayerRecord record = new PlayerRecord(event, UpdateSource.PORTRAIT);
			this.playerRecordRepo.saveAndFlush(record);
		}
	}
	
	@Transactional
	public void updatePlayerAmount(BalanceEvent event) {
		Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(this.cleanString(event.getPlayer()));
		if(maybeRecord.isPresent()) {
			PlayerRecord record = maybeRecord.get();
			record.setLastKnownAmount(event.getAmount());
			record.setUpdateSource(UpdateSource.PLAYER_AMOUNT);
			if(record.getHighestKnownAmount() == null || event.getAmount() > record.getHighestKnownAmount()) {
				record.setHighestKnownAmount(event.getAmount());
			}
			this.playerRecordRepo.saveAndFlush(record);
		} else {
			event.setPlayer(this.cleanString(event.getPlayer()));
			PlayerRecord record = new PlayerRecord(event, UpdateSource.PLAYER_AMOUNT);
			this.playerRecordRepo.saveAndFlush(record);
		}
	}
	
	@Transactional
	public void updatePlayerLevel(LevelUpEvent event) {
		String id = this.cleanString(event.getPlayer());
		event.setPlayer(id);
		Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(id);
		if(maybeRecord.isPresent()) {
			maybeRecord.get().setLastKnownLevel(event.getLevel());
			if(event instanceof ExpEvent) {
				Short remainingExp = ((ExpEvent)event).getRemainingExp();
				maybeRecord.get().setLastKnownRemainingExp(remainingExp);
				maybeRecord.get().setUpdateSource(UpdateSource.EXP);
			} else {
				maybeRecord.get().setLastKnownRemainingExp(GambleUtil.DEFAULT_REMAINING_EXP);
				maybeRecord.get().setUpdateSource(UpdateSource.PLAYER_LEVEL);
			}
			this.playerRecordRepo.saveAndFlush(maybeRecord.get());
		} else {
			PlayerRecord record = null;
			if(event instanceof ExpEvent) {
				ExpEvent expEvent = (ExpEvent) event;
				record = new PlayerRecord(expEvent, UpdateSource.EXP);
			} else {
				record = new PlayerRecord(event, UpdateSource.PLAYER_LEVEL);
			}
			
			this.playerRecordRepo.saveAndFlush(record);
		}
	}
	
	@Transactional
	public void updatePlayerAllegiance(AllegianceEvent event) {
		String id = this.cleanString(event.getPlayer());
		Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(id);
		if(maybeRecord.isPresent()) {
			maybeRecord.get().setAllegiance(event.getTeam());
			maybeRecord.get().setUpdateSource(UpdateSource.ALLEGIANCE);
			this.playerRecordRepo.saveAndFlush(maybeRecord.get());
		} else {
			event.setPlayer(this.cleanString(event.getPlayer()));
			PlayerRecord record = new PlayerRecord(event, UpdateSource.ALLEGIANCE);
			this.playerRecordRepo.saveAndFlush(record);
		}
	}
	
	@Transactional
	public void clearPlayerSkillsForPlayer(String player) {
		String id = this.cleanString(player);
		this.playerSkillRepo.deleteSkillsByPlayer(id);
		this.dumpService.getUserSkillsCache().put(id, new ArrayList<>());
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public void updatePlayerSkills(PlayerSkillEvent event) {
		String id = this.cleanString(event.getPlayer());
		Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(id);
		if(maybeRecord.isPresent()) {
			Hibernate.initialize(maybeRecord.get().getPlayerSkills());
			List<PlayerSkills> currentSkills = maybeRecord.get().getPlayerSkills();
			List<String> currentSkillNames = currentSkills.stream().map(playerSkill -> playerSkill.getSkill()).collect(Collectors.toList());
			//add skills to player
			for(PlayerSkills possibleNewSkill: event.getPlayerSkills()) {
				if(!currentSkillNames.contains(possibleNewSkill.getSkill())) {
					if(event instanceof PrestigeSkillsEvent) {
						maybeRecord.get().addPlayerSkill(possibleNewSkill.getSkill(), SkillType.PRESTIGE);
						this.playerRecordRepo.save(maybeRecord.get());
					} else {
						maybeRecord.get().addPlayerSkill(new PlayerSkills(possibleNewSkill.getSkill(), possibleNewSkill.getCooldown(), SkillType.USER, possibleNewSkill.getSkillCategory(), maybeRecord.get()));
						this.playerRecordRepo.save(maybeRecord.get());
					}
				} else if(currentSkillNames.contains(possibleNewSkill.getSkill())) { 
					if(!(event instanceof PrestigeSkillsEvent)) {
						List<PlayerSkills> matchingSkills = currentSkills.parallelStream().filter(playerSkill -> StringUtils.equalsIgnoreCase(playerSkill.getSkill(), possibleNewSkill.getSkill())).collect(Collectors.toList());
						if(matchingSkills.size() > 0) {
							PlayerSkills currentSkill = matchingSkills.get(0);
							if(currentSkill != null && possibleNewSkill.getCooldown() != null) {
								currentSkill.setCooldown(possibleNewSkill.getCooldown());
								currentSkill.setSkillCategory(possibleNewSkill.getSkillCategory());
								this.playerSkillRepo.save(currentSkill);
							}
						}
					}
				} else {
					if(event instanceof PrestigeSkillsEvent) {
						for(String skillName : event.getSkills()) {
							PlayerSkills playerSkill = this.playerSkillRepo.getSkillsByPlayerAndSkillName(id, skillName);
							if(playerSkill != null) {
								playerSkill.setSkillType(SkillType.PRESTIGE);
								this.playerSkillRepo.save(playerSkill);
							}
						}
						
					}
				}
			}
			
			this.playerRecordRepo.flush();
			this.playerSkillRepo.flush();
		}
	}
	
	@Transactional
	public void updatePlayerLastActive(LastActiveEvent event) {
		String id = this.cleanString(event.getPlayer());
		Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(id);
		if(maybeRecord.isPresent()) {
			maybeRecord.get().setLastActive(event.getLastActive());
			maybeRecord.get().setUpdateSource(UpdateSource.LAST_ACTIVE);
			this.playerRecordRepo.saveAndFlush(maybeRecord.get());
		} else {
			event.setPlayer(this.cleanString(event.getPlayer()));
			PlayerRecord record = new PlayerRecord(event, UpdateSource.LAST_ACTIVE);
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
		
		return;
	}
	
	@Transactional
	public void updateLastFightActive(FightEntryEvent event) {
		String id = this.cleanString(event.getPlayer());
		Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(id);
		if(maybeRecord.isPresent()) {
			maybeRecord.get().setLastFightActive(event.getEventTime());
			maybeRecord.get().setUpdateSource(UpdateSource.LAST_FIGHT_ACTIVE);
			this.playerRecordRepo.saveAndFlush(maybeRecord.get());
		} else {
			event.setPlayer(this.cleanString(event.getPlayer()));
			PlayerRecord record = new PlayerRecord(event, UpdateSource.LAST_FIGHT_ACTIVE);
			this.playerRecordRepo.saveAndFlush(record);
		}
	}
	
	@Transactional
	public void updateClassBonus(ClassBonusEvent classBonusEvent) {
		this.dumpService.getClassBonusRepo().deleteClassBonusForPlayer(classBonusEvent.getPlayer());
		this.dumpService.getClassBonusRepo().addClassBonusesForPlayer(classBonusEvent.getPlayer(), classBonusEvent.getClassBonuses());
	}
	
	@Transactional
	public void updateSkillBonus(SkillBonusEvent skillBonusEvent) {
		this.dumpService.getSkillBonusRepo().deleteSkillForPlayer(skillBonusEvent.getPlayer());
		this.dumpService.getSkillBonusRepo().addSkillBonusesForPlayer(skillBonusEvent.getPlayer(), skillBonusEvent.getSkillBonuses());
	}
	
	@Transactional
	public void updateSnub(SnubEvent event) {
		String id = this.cleanString(event.getPlayer());
		Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(id);
		if(maybeRecord.isPresent()) {
			maybeRecord.get().setSnubStreak(event.getSnub());
			maybeRecord.get().setUpdateSource(UpdateSource.SNUB);
			this.playerRecordRepo.saveAndFlush(maybeRecord.get());
		} else {
			event.setPlayer(this.cleanString(event.getPlayer()));
			PlayerRecord record = new PlayerRecord(event, UpdateSource.SNUB);
			this.playerRecordRepo.saveAndFlush(record);
		}
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public BattleGroundEvent generateSimulatedBalanceEvent(String player, int balanceUpdate, BalanceUpdateSource balanceUpdateSource) {
		OtherPlayerBalanceEvent event = null;
		
		String id = this.cleanString(player);
		Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(id);
		if(maybeRecord.isPresent()) {
			Integer currentKnownBalance = maybeRecord.get().getLastKnownAmount();
			if(currentKnownBalance == null) {
				currentKnownBalance = GambleUtil.getMinimumBetForBettor(maybeRecord.get().isSubscriber());
			}
			Integer newSimulatedAmount = currentKnownBalance + balanceUpdate;
			
			//if balance goes below the minimum for player, then return the minimum
			Integer minimumBalanceForPlayer = GambleUtil.getMinimumBetForLevel(maybeRecord.get().getLastKnownLevel(), maybeRecord.get().isSubscriber());
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
		String id = this.cleanString(event.getPlayer());
		BalanceHistory newBalance = new BalanceHistory(event);
		this.balanceHistoryRepo.saveAndFlush(newBalance);
		
		return;
	}
	
	protected String cleanString(String str) {
		String result = GambleUtil.cleanString(str);
		return result;
	}
	
}
