package fft_battleground.repo.dao;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import fft_battleground.botland.bot.util.BetterBetBot;
import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.cache.map.UserSkillsCache;
import fft_battleground.dump.cache.set.SoftDeleteCache;
import fft_battleground.event.BattleGroundEventBackPropagation;
import fft_battleground.event.detector.model.AllegianceEvent;
import fft_battleground.event.detector.model.BalanceEvent;
import fft_battleground.event.detector.model.BetEvent;
import fft_battleground.event.detector.model.ExpEvent;
import fft_battleground.event.detector.model.FightEntryEvent;
import fft_battleground.event.detector.model.LastActiveEvent;
import fft_battleground.event.detector.model.LevelUpEvent;
import fft_battleground.event.detector.model.PlayerSkillEvent;
import fft_battleground.event.detector.model.PortraitEvent;
import fft_battleground.event.detector.model.PrestigeSkillsEvent;
import fft_battleground.event.detector.model.SnubEvent;
import fft_battleground.event.detector.model.TeamInfoEvent;
import fft_battleground.event.detector.model.composite.OtherPlayerBalanceEvent;
import fft_battleground.event.detector.model.fake.ClassBonusEvent;
import fft_battleground.event.detector.model.fake.SkillBonusEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.exception.BattleGroundDataIntegrityViolationException;
import fft_battleground.exception.IncorrectTypeException;
import fft_battleground.repo.model.BalanceHistory;
import fft_battleground.repo.model.BotHourlyData;
import fft_battleground.repo.model.Bots;
import fft_battleground.repo.model.GlobalGilHistory;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.repo.model.PlayerSkills;
import fft_battleground.repo.model.PrestigeSkills;
import fft_battleground.repo.repository.BalanceHistoryRepo;
import fft_battleground.repo.repository.BotsHourlyDataRepo;
import fft_battleground.repo.repository.BotsRepo;
import fft_battleground.repo.repository.ClassBonusRepo;
import fft_battleground.repo.repository.GlobalGilHistoryRepo;
import fft_battleground.repo.repository.PlayerRecordRepo;
import fft_battleground.repo.repository.PlayerSkillRepo;
import fft_battleground.repo.repository.PrestigeSkillsRepo;
import fft_battleground.repo.repository.SkillBonusRepo;
import fft_battleground.repo.util.BalanceType;
import fft_battleground.repo.util.BalanceUpdateSource;
import fft_battleground.repo.util.UpdateSource;
import fft_battleground.skill.SkillUtils;
import fft_battleground.skill.model.SkillType;
import fft_battleground.util.GambleUtil;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RepoTransactionManager {

	@Autowired
	private PlayerRecordRepo playerRecordRepo;
	
	@Autowired
	private BalanceHistoryRepo balanceHistoryRepo;
	
	@Autowired
	private GlobalGilHistoryRepo globalGilHistoryRepo;
	
	@Autowired
	private PlayerSkillRepo playerSkillRepo;
	
	@Autowired
	private PrestigeSkillsRepo prestigeSkillRepo;
	
	@Autowired
	private BotsRepo botsRepo;
	
	@Autowired
	private BotsHourlyDataRepo botsHourlyDataRepo;
	
	@Autowired
	private ClassBonusRepo classBonusRepo;
	
	@Autowired
	private SkillBonusRepo skillBonusRepo;
	
	@Autowired
	private BattleGroundEventBackPropagation battleGroundEventBackPropagation;
	
	@Autowired
	private WebhookManager errorWebhookManager;
	
	@Autowired
	private UserSkillsCache userSkillsCache;

	@Autowired
	private SoftDeleteCache softDeleteCache;
	
	public RepoTransactionManager() {}
	
	public RepoTransactionManager(PlayerRecordRepo playerRecordRepo, BalanceHistoryRepo balanceHistoryRepo,
			BattleGroundEventBackPropagation battleGroundEventBackPropagation) {
		this.playerRecordRepo = playerRecordRepo;
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
			String cleanedName = this.cleanString(bet.getPlayer());
			PlayerRecord player = null;
			try {
				Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(cleanedName);
				if(!maybeRecord.isPresent()) {
					log.info("creating new account {}", cleanedName);
					player = new PlayerRecord(this.cleanString(bet.getPlayer()), 0, 1, bet.getIsSubscriber(), UpdateSource.REPORT_AS_LOSS);
				} else if(this.softDeleteCache.contains(cleanedName)) { 
					this.playerRecordRepo.unDeletePlayer(cleanedName);
					maybeRecord = this.playerRecordRepo.findById(cleanedName);
					if(maybeRecord.isPresent()) {
						player = maybeRecord.get();
						player.setLosses(player.getLosses() + 1);
						player.setUpdateSource(UpdateSource.REPORT_AS_LOSS);
						player.setIsSubscriber(bet.getIsSubscriber());
						
						this.softDeleteCache.remove(cleanedName);
						log.info("undeleting account {}", cleanedName);
					}
				} else {
					player = maybeRecord.get();
					player.setLosses(player.getLosses() + 1);
					player.setUpdateSource(UpdateSource.REPORT_AS_LOSS);
					player.setIsSubscriber(bet.getIsSubscriber());
				}
				this.playerRecordRepo.saveAndFlush(player);
				
				//now let's backpropagate some simulated balance updates
				sendBalanceBackpropagation(bet, teamBettingOdds, false);
			} catch(DataIntegrityViolationException dive) {
				String errorMessage = "data integrity exception found for player " + cleanedName;
				log.warn(errorMessage, dive);
				this.errorWebhookManager.sendException(dive, errorMessage);
			}
		}
		
		log.info("Successfully reported {} bet losses", bets.size());;
		
	}
	
	@Transactional
	public void reportAsFightLoss(TeamInfoEvent team) {
		for(Pair<String, String> teamMemberInfo: team.getPlayerUnitPairs()) {
			String cleanedName = this.cleanString(teamMemberInfo.getLeft());
			try {
				Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(cleanedName);
				if(maybeRecord.isPresent()) {
					PlayerRecord record = maybeRecord.get();
					Integer currentFightLosses = record.getFightLosses();
					currentFightLosses = currentFightLosses + 1;
					record.setFightLosses(currentFightLosses);
					record.setUpdateSource(UpdateSource.REPORT_AS_FIGHT_LOSS);
					
					this.playerRecordRepo.saveAndFlush(record);
				} else if(this.softDeleteCache.contains(cleanedName)) { 
					this.playerRecordRepo.unDeletePlayer(cleanedName);
					maybeRecord = this.playerRecordRepo.findById(cleanedName);
					if(maybeRecord.isPresent()) {
						PlayerRecord record = maybeRecord.get();
						Integer currentFightLosses = record.getFightLosses();
						currentFightLosses = currentFightLosses + 1;
						record.setFightLosses(currentFightLosses);
						record.setUpdateSource(UpdateSource.REPORT_AS_FIGHT_LOSS);
						this.playerRecordRepo.saveAndFlush(record);
						this.softDeleteCache.remove(cleanedName);
						log.info("undeleting account {}", cleanedName);
					}
				}else {
					PlayerRecord record = new PlayerRecord(this.cleanString(teamMemberInfo.getLeft()), UpdateSource.REPORT_AS_FIGHT_LOSS);
					record.setFightLosses(1);
					record.setFightWins(0);
					this.playerRecordRepo.saveAndFlush(record);
					log.info("creating new account {}", cleanedName);
				}
			} catch(DataIntegrityViolationException dive) {
				String errorMessage = "data integrity exception found for player " + cleanedName;
				log.warn(errorMessage, dive);
				this.errorWebhookManager.sendException(dive, errorMessage);
			}
		}
		
	}
	
	@Transactional
	public void reportAsWin(List<BetEvent> bets, Float teamBettingOdds) {
		for(BetEvent bet : bets) {
			PlayerRecord player = null;
			String cleanedName = this.cleanString(bet.getPlayer());
			try {
				Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(cleanedName);
				if(!maybeRecord.isPresent()) {
					player = new PlayerRecord(this.cleanString(bet.getPlayer()), 1, 0, bet.getIsSubscriber(), UpdateSource.REPORT_AS_WIN);
					log.info("creating new account {}", cleanedName);
				} else if(this.softDeleteCache.contains(cleanedName)) {
					this.playerRecordRepo.unDeletePlayer(cleanedName);
					maybeRecord = this.playerRecordRepo.findById(cleanedName);
					if(maybeRecord.isPresent()) {
						player = maybeRecord.get();
						player.setWins(player.getWins() + 1);
						player.setUpdateSource(UpdateSource.REPORT_AS_WIN);
						player.setIsSubscriber(bet.getIsSubscriber());
						
						this.softDeleteCache.remove(cleanedName);
						log.info("undeleting account {}", cleanedName);
					}
				} else {
					player = maybeRecord.get();
					player.setWins(player.getWins() + 1);
					player.setUpdateSource(UpdateSource.REPORT_AS_WIN);
					player.setIsSubscriber(bet.getIsSubscriber());
				}
				
				this.playerRecordRepo.saveAndFlush(player);
				
				//now let's backpropagate some simulated balance updates
				sendBalanceBackpropagation(bet, teamBettingOdds, true);
			} catch(DataIntegrityViolationException dive) {
				String errorMessage = "data integrity exception found for player " + cleanedName;
				log.warn(errorMessage, dive);
				this.errorWebhookManager.sendException(dive, errorMessage);
			}
		}
		
		log.info("Successfully reported {} bet wins", bets.size());
	}
	
	@Transactional
	public void reportAsFightWin(TeamInfoEvent team) {
		for(Pair<String, String> teamMemberInfo: team.getPlayerUnitPairs()) {
			String cleanedName = this.cleanString(teamMemberInfo.getLeft());
			try {
				
				Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(cleanedName);
				if(maybeRecord.isPresent()) {
					PlayerRecord record = maybeRecord.get();
					Integer currentFightWins = record.getFightWins();
					currentFightWins = currentFightWins + 1;
					record.setFightWins(currentFightWins);
					record.setUpdateSource(UpdateSource.REPORT_AS_FIGHT_WIN);
					
					this.playerRecordRepo.saveAndFlush(record);
				} else if(this.softDeleteCache.contains(cleanedName)) {
					this.playerRecordRepo.unDeletePlayer(cleanedName);
					maybeRecord = this.playerRecordRepo.findById(cleanedName);
					if(maybeRecord.isPresent()) {
						PlayerRecord record = maybeRecord.get();
						Integer currentFightWins = record.getFightWins();
						currentFightWins = currentFightWins + 1;
						record.setFightWins(currentFightWins);
						record.setUpdateSource(UpdateSource.REPORT_AS_FIGHT_WIN);
						
						this.softDeleteCache.remove(cleanedName);
						log.info("undeleting account {}", cleanedName);
						this.playerRecordRepo.saveAndFlush(record);
					}
				}  else {
					PlayerRecord record = new PlayerRecord(this.cleanString(teamMemberInfo.getLeft()), UpdateSource.REPORT_AS_FIGHT_WIN);
					record.setFightWins(1);
					record.setFightLosses(0);
					this.playerRecordRepo.saveAndFlush(record);
					log.info("creating new account {}", cleanedName);
				}
			} catch(DataIntegrityViolationException dive) {
				String errorMessage = "data integrity exception found for player " + cleanedName;
				log.warn(errorMessage, dive);
				this.errorWebhookManager.sendException(dive, errorMessage);
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
	public void updatePlayerPortrait(PortraitEvent event) throws IncorrectTypeException, BattleGroundDataIntegrityViolationException {
		String cleanedName = this.cleanString(event.getPlayer());
		if(StringUtils.isNumeric(event.getPortrait())) {
			throw new IncorrectTypeException("The portrait " + event.getPortrait() + " for player " + event.getPlayer() + " is actually a number");
		}
		try {
			Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(cleanedName);
			if(maybeRecord.isPresent()) {
				PlayerRecord record = maybeRecord.get();
				record.setPortrait(event.getPortrait());
				record.setUpdateSource(UpdateSource.PORTRAIT);
				this.playerRecordRepo.saveAndFlush(record);
			} else if(this.softDeleteCache.contains(cleanedName)){
				this.playerRecordRepo.unDeletePlayer(cleanedName);
				maybeRecord = this.playerRecordRepo.findById(cleanedName);
				if(maybeRecord.isPresent()) {
					PlayerRecord record = maybeRecord.get();
					record.setPortrait(event.getPortrait());
					record.setUpdateSource(UpdateSource.PORTRAIT);
					this.playerRecordRepo.saveAndFlush(record);
					this.softDeleteCache.remove(cleanedName);
					log.info("undeleting account {}", cleanedName);
				}
			}else {
				event.setPlayer(this.cleanString(event.getPlayer()));
				PlayerRecord record = new PlayerRecord(event, UpdateSource.PORTRAIT);
				this.playerRecordRepo.saveAndFlush(record);
				log.info("creating new account {}", cleanedName);
			}
		} catch(DataIntegrityViolationException dive) {
			throw new BattleGroundDataIntegrityViolationException(cleanedName, dive);
		}
	}
	
	@Transactional
	public void updatePlayerAmount(BalanceEvent event) throws BattleGroundDataIntegrityViolationException {
		String cleanedName = this.cleanString(event.getPlayer());
		try {
			Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(cleanedName);
			if(maybeRecord.isPresent()) {
				PlayerRecord record = maybeRecord.get();
				record.setLastKnownAmount(event.getAmount());
				record.setUpdateSource(UpdateSource.PLAYER_AMOUNT);
				if(record.getHighestKnownAmount() == null || event.getAmount() > record.getHighestKnownAmount()) {
					record.setHighestKnownAmount(event.getAmount());
				}
				this.playerRecordRepo.saveAndFlush(record);
			} else if(this.softDeleteCache.contains(cleanedName)) {
				this.playerRecordRepo.unDeletePlayer(cleanedName);
				maybeRecord = this.playerRecordRepo.findById(cleanedName);
				if(maybeRecord.isPresent()) {
					PlayerRecord record = maybeRecord.get();
					record.setLastKnownAmount(event.getAmount());
					record.setUpdateSource(UpdateSource.PLAYER_AMOUNT);
					if(record.getHighestKnownAmount() == null || event.getAmount() > record.getHighestKnownAmount()) {
						record.setHighestKnownAmount(event.getAmount());
					}
					this.playerRecordRepo.saveAndFlush(record);
					this.softDeleteCache.remove(cleanedName);
					log.info("undeleting account {}", cleanedName);
				}
			}else {
				event.setPlayer(this.cleanString(event.getPlayer()));
				PlayerRecord record = new PlayerRecord(event, UpdateSource.PLAYER_AMOUNT);
				this.playerRecordRepo.saveAndFlush(record);
				log.info("creating new account {}", cleanedName);
			}
		} catch(DataIntegrityViolationException dive) {
			throw new BattleGroundDataIntegrityViolationException(cleanedName, dive);
		}
	}
	
	@Transactional
	public void updatePlayerLevel(LevelUpEvent event) throws BattleGroundDataIntegrityViolationException {
		String cleanedName = this.cleanString(event.getPlayer());
		event.setPlayer(cleanedName);
		try {
			Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(cleanedName);
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
			} else if(this.softDeleteCache.contains(cleanedName)) {
				this.playerRecordRepo.unDeletePlayer(cleanedName);
				maybeRecord = this.playerRecordRepo.findById(cleanedName);
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
					this.softDeleteCache.remove(cleanedName);
					log.info("undeleting account {}", cleanedName);
				}
			} else {
				PlayerRecord record = null;
				if(event instanceof ExpEvent) {
					ExpEvent expEvent = (ExpEvent) event;
					record = new PlayerRecord(expEvent, UpdateSource.EXP);
				} else {
					record = new PlayerRecord(event, UpdateSource.PLAYER_LEVEL);
				}
				log.info("creating new account {}", cleanedName);
				
				this.playerRecordRepo.saveAndFlush(record);
			}
		} catch(DataIntegrityViolationException dive) {
			throw new BattleGroundDataIntegrityViolationException(cleanedName, dive);
		}
	}
	
	@Transactional
	public void updatePlayerAllegiance(AllegianceEvent event) throws BattleGroundDataIntegrityViolationException {
		String cleanedName = this.cleanString(event.getPlayer());
		try {
			Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(cleanedName);
			if(maybeRecord.isPresent()) {
				maybeRecord.get().setAllegiance(event.getTeam());
				maybeRecord.get().setUpdateSource(UpdateSource.ALLEGIANCE);
				this.playerRecordRepo.saveAndFlush(maybeRecord.get());
			} else if(this.softDeleteCache.contains(cleanedName)) {
				this.playerRecordRepo.unDeletePlayer(cleanedName);
				maybeRecord = this.playerRecordRepo.findById(cleanedName);
				if(maybeRecord.isPresent()) {
					maybeRecord.get().setAllegiance(event.getTeam());
					maybeRecord.get().setUpdateSource(UpdateSource.ALLEGIANCE);
					this.playerRecordRepo.saveAndFlush(maybeRecord.get());
					this.softDeleteCache.remove(cleanedName);
					log.info("undeleting account {}", cleanedName);
				}
			}else {
				event.setPlayer(this.cleanString(event.getPlayer()));
				PlayerRecord record = new PlayerRecord(event, UpdateSource.ALLEGIANCE);
				this.playerRecordRepo.saveAndFlush(record);
				log.info("creating new account {}", cleanedName);
			}
		} catch(DataIntegrityViolationException dive) {
			throw new BattleGroundDataIntegrityViolationException(cleanedName, dive);
		}
	}
	
	@Transactional
	public void clearPlayerSkillsForPlayer(String player) {
		String id = this.cleanString(player);
		this.playerSkillRepo.deleteSkillsByPlayer(id);
		this.userSkillsCache.put(id, new ArrayList<>());
	}
	
	@Transactional
	public void softDeletePlayerAccount(List<String> badAccounts) {
		for(String account : badAccounts) {
			this.playerRecordRepo.softDeletePlayer(account);
			this.softDeleteCache.add(account);		
		}
	}
	
	@Transactional
	public void undeletePlayerAccounts(List<String> falselyFlaggedAccounts) {
		for(String account: falselyFlaggedAccounts) {
			this.playerRecordRepo.unDeletePlayer(account);
			this.softDeleteCache.remove(account);
		}
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public void updatePlayerSkills(PlayerSkillEvent event) throws BattleGroundDataIntegrityViolationException {
		String cleanedName = this.cleanString(event.getPlayer());
		try {
			Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(cleanedName);
			if(maybeRecord.isPresent()) {
				Hibernate.initialize(maybeRecord.get().getPlayerSkills());
				List<PlayerSkills> currentSkills = maybeRecord.get().getPlayerSkills();
				List<String> currentSkillNames = currentSkills.stream().map(playerSkill -> playerSkill.getSkill()).collect(Collectors.toList());
				//add skills to player
				for(PlayerSkills possibleNewSkill: event.getPlayerSkills()) {
					if(!currentSkillNames.contains(possibleNewSkill.getSkill())) {
						maybeRecord.get().addPlayerSkill(new PlayerSkills(possibleNewSkill.getSkill(), possibleNewSkill.getCooldown(), SkillType.USER, possibleNewSkill.getSkillCategory(), maybeRecord.get()));
						this.playerRecordRepo.save(maybeRecord.get());
					} else if(currentSkillNames.contains(possibleNewSkill.getSkill())) { 
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
				}
				this.playerRecordRepo.flush();
				this.playerSkillRepo.flush();
			}
		} catch(DataIntegrityViolationException dive) {
			throw new BattleGroundDataIntegrityViolationException(cleanedName, dive);
		}
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public void updatePrestigeSkills(PrestigeSkillsEvent event) throws BattleGroundDataIntegrityViolationException {
		String cleanedName = this.cleanString(event.getPlayer());
		if(event.getPlayerSkills().size() > SkillUtils.PRESTIGE_SKILLS.size()) {
			throw new DataIntegrityViolationException(MessageFormat.format("Attempting to push {0} prestige skills for user {1}, but the limit is {3}", 
					event.getPlayerSkills().size(), cleanedName, SkillUtils.PRESTIGE_SKILLS.size()));
		}
		try {
			Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(cleanedName);
			if(maybeRecord.isPresent()) {
				Hibernate.initialize(maybeRecord.get().getPrestigeSkills());
				List<PrestigeSkills> currentSkills = maybeRecord.get().getPrestigeSkills();
				List<String> currentSkillNames = currentSkills.stream().map(playerSkill -> playerSkill.getSkill()).collect(Collectors.toList());
				//add skills to player
				for(PrestigeSkills possibleNewSkill: event.getPlayerSkills()) {
					if(!currentSkillNames.contains(possibleNewSkill.getSkill())) {
						maybeRecord.get().addPrestigeSkills(new PrestigeSkills(possibleNewSkill.getSkill(), possibleNewSkill.getCooldown(), SkillType.USER, possibleNewSkill.getSkillCategory(), maybeRecord.get()));
						this.playerRecordRepo.save(maybeRecord.get());
					} else if(currentSkillNames.contains(possibleNewSkill.getSkill())) { 
						List<PrestigeSkills> matchingSkills = currentSkills.parallelStream().filter(playerSkill -> StringUtils.equalsIgnoreCase(playerSkill.getSkill(), possibleNewSkill.getSkill())).collect(Collectors.toList());
						if(matchingSkills.size() > 0) {
							PrestigeSkills currentSkill = matchingSkills.get(0);
							if(currentSkill != null && possibleNewSkill.getCooldown() != null) {
								currentSkill.setCooldown(possibleNewSkill.getCooldown());
								currentSkill.setSkillCategory(possibleNewSkill.getSkillCategory());
								this.prestigeSkillRepo.save(currentSkill);
							}
						}
					} 
				}
				this.playerRecordRepo.flush();
				this.prestigeSkillRepo.flush();
			}
		} catch(DataIntegrityViolationException dive) {
			throw new BattleGroundDataIntegrityViolationException(cleanedName, dive);
		}
	}
	
	@Transactional
	public void updatePlayerLastActive(LastActiveEvent event) throws BattleGroundDataIntegrityViolationException {
		String cleanedName = this.cleanString(event.getPlayer());
		try {
			Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(cleanedName);
			if(maybeRecord.isPresent()) {
				maybeRecord.get().setLastActive(event.getLastActive());
				maybeRecord.get().setUpdateSource(UpdateSource.LAST_ACTIVE);
				this.playerRecordRepo.saveAndFlush(maybeRecord.get());
			} else if(this.softDeleteCache.contains(cleanedName)) {
				this.playerRecordRepo.unDeletePlayer(cleanedName);
				maybeRecord = this.playerRecordRepo.findById(cleanedName);
				if(maybeRecord.isPresent()) {
					maybeRecord.get().setLastActive(event.getLastActive());
					maybeRecord.get().setUpdateSource(UpdateSource.LAST_ACTIVE);
					this.playerRecordRepo.saveAndFlush(maybeRecord.get());
					this.softDeleteCache.remove(cleanedName);
					log.info("undeleting account {}", cleanedName);
				}
			}else {
				event.setPlayer(this.cleanString(event.getPlayer()));
				PlayerRecord record = new PlayerRecord(event, UpdateSource.LAST_ACTIVE);
				this.playerRecordRepo.saveAndFlush(record);
				log.info("creating new account {}", cleanedName);
			}
		} catch(DataIntegrityViolationException dive) {
			throw new BattleGroundDataIntegrityViolationException(cleanedName, dive);
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
	public void updateLastFightActive(FightEntryEvent event) throws BattleGroundDataIntegrityViolationException {
		String cleanedName = this.cleanString(event.getPlayer());
		try {
			Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(cleanedName);
			if(maybeRecord.isPresent()) {
				maybeRecord.get().setLastFightActive(event.getEventTime());
				maybeRecord.get().setUpdateSource(UpdateSource.LAST_FIGHT_ACTIVE);
				this.playerRecordRepo.saveAndFlush(maybeRecord.get());
			} else if(this.softDeleteCache.contains(cleanedName)) {
				maybeRecord = this.playerRecordRepo.findById(cleanedName);
				if(maybeRecord.isPresent()) {
					maybeRecord.get().setLastFightActive(event.getEventTime());
					maybeRecord.get().setUpdateSource(UpdateSource.LAST_FIGHT_ACTIVE);
					this.playerRecordRepo.saveAndFlush(maybeRecord.get());
					this.softDeleteCache.remove(cleanedName);
					log.info("undeleting account {}", cleanedName);
				}
			}else {
				event.setPlayer(this.cleanString(event.getPlayer()));
				PlayerRecord record = new PlayerRecord(event, UpdateSource.LAST_FIGHT_ACTIVE);
				this.playerRecordRepo.saveAndFlush(record);
				log.info("creating new account {}", cleanedName);
			}
		} catch(DataIntegrityViolationException dive) {
			throw new BattleGroundDataIntegrityViolationException(cleanedName, dive);
		}
	}
	
	@Transactional
	public void updateClassBonus(ClassBonusEvent classBonusEvent) throws BattleGroundDataIntegrityViolationException {
		try {
			this.classBonusRepo.deleteClassBonusForPlayer(classBonusEvent.getPlayer());
			this.classBonusRepo.addClassBonusesForPlayer(classBonusEvent.getPlayer(), classBonusEvent.getClassBonuses());
		} catch(DataIntegrityViolationException dive) {
			String errorMessage = "Data integrity violation during class bonus update for player " + classBonusEvent.getPlayer();
			log.error(errorMessage, dive);
			throw new BattleGroundDataIntegrityViolationException(errorMessage, dive);
		}
	}
	
	@Transactional
	public void updateSkillBonus(SkillBonusEvent skillBonusEvent) throws BattleGroundDataIntegrityViolationException {
		try {
			this.skillBonusRepo.deleteSkillForPlayer(skillBonusEvent.getPlayer());
			this.skillBonusRepo.addSkillBonusesForPlayer(skillBonusEvent.getPlayer(), skillBonusEvent.getSkillBonuses());
		} catch(DataIntegrityViolationException dive) {
			String errorMessage = "Data integrity violation during skill bonus update for player " + skillBonusEvent.getPlayer();
			log.error(errorMessage, dive);
			throw new BattleGroundDataIntegrityViolationException(errorMessage, dive);
		}
	}
	
	@Transactional
	public void updateSnub(SnubEvent event) throws BattleGroundDataIntegrityViolationException {
		String cleanedName = this.cleanString(event.getPlayer());
		try {
			Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(cleanedName);
			if(maybeRecord.isPresent()) {
				maybeRecord.get().setSnubStreak(event.getSnub());
				maybeRecord.get().setUpdateSource(UpdateSource.SNUB);
				this.playerRecordRepo.saveAndFlush(maybeRecord.get());
			} else if(this.softDeleteCache.contains(cleanedName)) {
				maybeRecord = this.playerRecordRepo.findById(cleanedName);
				if(maybeRecord.isPresent()) {
					maybeRecord.get().setSnubStreak(event.getSnub());
					maybeRecord.get().setUpdateSource(UpdateSource.SNUB);
					this.playerRecordRepo.saveAndFlush(maybeRecord.get());
					this.softDeleteCache.remove(cleanedName);
					log.info("undeleting account {}", cleanedName);
				}
			} else {
				event.setPlayer(this.cleanString(event.getPlayer()));
				PlayerRecord record = new PlayerRecord(event, UpdateSource.SNUB);
				this.playerRecordRepo.saveAndFlush(record);
				log.info("creating new account {}", cleanedName);
			}
		} catch(DataIntegrityViolationException dive) {
			throw new BattleGroundDataIntegrityViolationException(cleanedName, dive);
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
