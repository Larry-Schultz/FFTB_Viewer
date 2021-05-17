package fft_battleground.repo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fft_battleground.botland.BetterBetBot;
import fft_battleground.botland.model.BalanceUpdateSource;
import fft_battleground.botland.model.BetResults;
import fft_battleground.botland.model.DatabaseResultsData;
import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.DumpService;
import fft_battleground.event.BattleGroundEventBackPropagation;
import fft_battleground.event.PlayerSkillRefresh;
import fft_battleground.event.model.AllegianceEvent;
import fft_battleground.event.model.BalanceEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BuySkillEvent;
import fft_battleground.event.model.BuySkillRandomEvent;
import fft_battleground.event.model.ExpEvent;
import fft_battleground.event.model.FightEntryEvent;
import fft_battleground.event.model.GiftSkillEvent;
import fft_battleground.event.model.LastActiveEvent;
import fft_battleground.event.model.LevelUpEvent;
import fft_battleground.event.model.OtherPlayerBalanceEvent;
import fft_battleground.event.model.OtherPlayerExpEvent;
import fft_battleground.event.model.OtherPlayerSnubEvent;
import fft_battleground.event.model.PlayerSkillEvent;
import fft_battleground.event.model.PortraitEvent;
import fft_battleground.event.model.PrestigeAscensionEvent;
import fft_battleground.event.model.PrestigeSkillsEvent;
import fft_battleground.event.model.SkillWinEvent;
import fft_battleground.event.model.SnubEvent;
import fft_battleground.event.model.fake.ClassBonusEvent;
import fft_battleground.event.model.fake.GlobalGilHistoryUpdateEvent;
import fft_battleground.event.model.fake.SkillBonusEvent;
import fft_battleground.exception.IncorrectTypeException;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.util.GambleUtil;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RepoManager extends Thread {

	@Autowired
	private RepoTransactionManager repoTransactionManager;
	
	@Autowired
	private BlockingQueue<DatabaseResultsData> betResultsQueue;
	
	@Autowired
	private BattleGroundEventBackPropagation battleGroundEventBackPropagation;
	
	@Autowired
	private DumpService dumpService;
	
	@Autowired
	private WebhookManager errorWebhookManager;
	
	@Autowired
	private WebhookManager ascensionWebhookManager;
	
	public RepoManager() {
		this.setName("RepoManagerThread");
	}
	
	public RepoTransactionManager getRepoTransactionManager() {
		return this.repoTransactionManager;
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
				} else if(newResults instanceof PlayerSkillRefresh) {
					this.handlePlayerSkillRefresh((PlayerSkillRefresh) newResults);
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
				} else if(newResults instanceof GlobalGilHistoryUpdateEvent) {
					this.handleGlobalGilHistoryUpdateEvent((GlobalGilHistoryUpdateEvent) newResults);
				} else if(newResults instanceof PrestigeAscensionEvent) {
					this.handlePrestigeAscensionEvent((PrestigeAscensionEvent) newResults);
				} else if(newResults instanceof FightEntryEvent) {
					this.handleFightEntryEvent((FightEntryEvent)newResults);
				} else if(newResults instanceof BuySkillRandomEvent) {
					this.handleBuySkillRandomEvent((BuySkillRandomEvent) newResults);
				} else if(newResults instanceof ClassBonusEvent) {
					this.handleClassBonusEvent((ClassBonusEvent) newResults);
				} else if(newResults instanceof SkillBonusEvent) {
					this.handleClassBonusEvent((SkillBonusEvent) newResults);
				} else if(newResults instanceof SnubEvent) {
					this.handleSnubEvent((SnubEvent) newResults);
				} else if(newResults instanceof OtherPlayerSnubEvent) {
					this.handleOtherPlayerSnubEvent((OtherPlayerSnubEvent) newResults);
				}
			} catch (InterruptedException e) {
				log.error("error in RepoManager", e);
			} catch(IncorrectTypeException e) {
				log.error("A value in the repo was incorrectly typed with error: {}", e.getMessage(), e);
				errorWebhookManager.sendException(e);
			} catch(Exception e) {
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
		
		try {
			//update player data
			this.updatePlayerData(newResults);
			
			//update match data
			this.repoTransactionManager.updateMatchData(newResults);
			
			//update bot data
			this.updateBotData(newResults);
		} catch( NullPointerException e) {
			log.error("There was an error updating player data due to a NullPointException, skipping match data update", e);
		}
	}
	
	protected void handlePortraitEvent(PortraitEvent event) throws IncorrectTypeException {
		this.repoTransactionManager.updatePlayerPortrait(event);
	}
	
	protected void handleSkillWinEvent(SkillWinEvent event) {
		for(PlayerSkillEvent skillEvent: event.getSkillEvents()) {
			this.repoTransactionManager.updatePlayerSkills(skillEvent);
		}
	}
	
	protected void handleOtherPlayerBalanceEvent(OtherPlayerBalanceEvent event) {
		if(event != null && event.getOtherPlayerBalanceEvents() != null) {
			for(BalanceEvent balanceEvent : event.getOtherPlayerBalanceEvents()) {
				this.repoTransactionManager.updatePlayerAmount(balanceEvent);
				this.repoTransactionManager.addEntryToBalanceHistory(balanceEvent);
				this.dumpService.updateBalanceCache(balanceEvent);
			}
		}
	}
	
	protected void handleLevelUpEvent(LevelUpEvent event) {
		this.repoTransactionManager.updatePlayerLevel(event);
		if(event.getSkill() != null && event.getSkill().getSkills().size() > 0) {
			this.repoTransactionManager.updatePlayerSkills(event.getSkill());
		}
	}
	
	protected void handleOtherPlayerExpEvent(OtherPlayerExpEvent event) {
		for(ExpEvent expEvent : event.getExpEvents()) {
			this.repoTransactionManager.updatePlayerLevel((LevelUpEvent) expEvent);
		}
	}
	
	protected void handleAllegianceEvent(AllegianceEvent event) {
		this.repoTransactionManager.updatePlayerAllegiance(event);
	}
	
	protected void handlePrestigeSkillsEvent(PrestigeSkillsEvent event) {
		this.handlePlayerSkillEvent((PlayerSkillEvent) event);
	}
	
	protected void handlePlayerSkillEvent(PlayerSkillEvent event) {
		this.repoTransactionManager.updatePlayerSkills(event);
	}
	
	protected void handlePlayerSkillRefresh(PlayerSkillRefresh event) {
		try {
			this.repoTransactionManager.clearPlayerSkillsForPlayer(event.getPlayer());
			this.repoTransactionManager.updatePlayerSkills(event.getPlayerSkillEvent());
			if(event.getPrestigeSkillEvent() != null) {
				this.repoTransactionManager.updatePlayerSkills(event.getPrestigeSkillEvent());
			}
		} catch(Exception e) {
			log.error("Error processing Ascension refresh for player {}", event.getPlayer());
			this.errorWebhookManager.sendException(e, "error processing player skill refresh");
		}
	}
	
	protected void handleGlobalGilHistoryUpdateEvent(GlobalGilHistoryUpdateEvent newResults) {
		this.repoTransactionManager.updateGlobalGilHistory(newResults.getGlobalGilHistory());
	}
	
	protected void handlePrestigeAscensionEvent(final PrestigeAscensionEvent event) {
		String id = GambleUtil.cleanString(event.getPrestigeSkillsEvent().getPlayer());
		if(event.getCurrentBalance() != null) {
			this.repoTransactionManager.generateSimulatedBalanceEvent(event.getPrestigeSkillsEvent().getPlayer(), (-1) * event.getCurrentBalance(), BalanceUpdateSource.PRESTIGE);
		}
		this.handlePrestigeSkillsEvent(event.getPrestigeSkillsEvent());
		//use Timer to force update player skill.  May delay events behind this propagation
		try {
			int prestigeBefore = this.dumpService.getPrestigeSkillsCache().get(id) != null ? this.dumpService.getPrestigeSkillsCache().get(id).size() : 0;
			this.ascensionWebhookManager.sendAscensionMessage(id, prestigeBefore, prestigeBefore + 1);
			List<String> userSkills = new ArrayList<>();
			Set<String> prestigeSkills = new HashSet<>(this.dumpService.getPrestigeSkillsCache().get(id));
			if(prestigeSkills != null && event.getPrestigeSkillsEvent() != null && event.getPrestigeSkillsEvent().getSkills() != null && event.getPrestigeSkillsEvent().getSkills().size() > 0) {
				prestigeSkills.add(event.getPrestigeSkillsEvent().getSkills().get(0));
			}
			
			List<String> uniquePrestigeSkillList = new ArrayList<>(prestigeSkills);
			
			this.dumpService.getUserSkillsCache().put(id, userSkills);
			this.dumpService.getPrestigeSkillsCache().put(id, uniquePrestigeSkillList);
			
			PlayerSkillRefresh refresh = new PlayerSkillRefresh(id, userSkills, uniquePrestigeSkillList, event);
			this.handlePlayerSkillRefresh(refresh);
		} catch (Exception e) {
			log.error("Error processing Ascension refresh for player {}", id);
			this.errorWebhookManager.sendException(e);
		}
	}
	
	@SneakyThrows
	protected void handleBuySkillEvent(BuySkillEvent event) {
		for(PlayerSkillEvent playerSkillEvent : event.getSkillEvents()) {
			this.handlePlayerSkillEvent(playerSkillEvent);
			BattleGroundEvent balanceUpdate = this.repoTransactionManager.generateSimulatedBalanceEvent(playerSkillEvent.getPlayer(), BuySkillEvent.skillBuyBalanceUpdate, BalanceUpdateSource.BUYSKILL);
			if(balanceUpdate != null) {
				this.battleGroundEventBackPropagation.SendUnitThroughTimer(balanceUpdate);
			}
		}
	}
	
	@SneakyThrows
	private void handleBuySkillRandomEvent(BuySkillRandomEvent event) {
		this.handlePlayerSkillEvent(event.getSkillEvent());
		BattleGroundEvent balanceUpdate = this.repoTransactionManager.generateSimulatedBalanceEvent(event.getPlayer(), event.getSkillBuyBalanceUpdate(), BalanceUpdateSource.BUYSKILL);
		if(balanceUpdate != null) {
			this.battleGroundEventBackPropagation.SendUnitThroughTimer(balanceUpdate);
		}
	}
	
	protected void handleLastActiveSkillEvent(LastActiveEvent event) {
		this.repoTransactionManager.updatePlayerLastActive(event);
	}
	
	private void handleFightEntryEvent(FightEntryEvent newResults) {
		this.repoTransactionManager.updateLastFightActive(newResults);
	}
	
	private void handleGiftSkillEvent(GiftSkillEvent newResults) {
		for(int i = 0; i < newResults.getGiftSkills().size(); i++) {
			this.repoTransactionManager.updatePlayerSkills(newResults.getGiftSkills().get(i).getPlayerSkillEvent());
			this.repoTransactionManager.generateSimulatedBalanceEvent(newResults.getGiftSkills().get(i).getGivingPlayer(), newResults.getCost(), BalanceUpdateSource.GIFTSKILL);
		}
	}
	
	private void handleClassBonusEvent(ClassBonusEvent newResults) {
		this.repoTransactionManager.updateClassBonus(newResults);
	}
	
	private void handleClassBonusEvent(SkillBonusEvent newResults) {
		
		this.repoTransactionManager.updateSkillBonus(newResults);
	}
	
	private void handleSnubEvent(SnubEvent newResults) {
		this.repoTransactionManager.updateSnub(newResults);
	}
	
	private void handleOtherPlayerSnubEvent(OtherPlayerSnubEvent newResults) {
		for(SnubEvent event: newResults.getSnubEvents()) {
			this.repoTransactionManager.updateSnub(event);
		}
	}

	
	protected void updatePlayerData(BetResults newResults) {
		
		Float leftTeamOdds = GambleUtil.bettingOdds(newResults.getFullBettingData().getTeam1Amount(), newResults.getFullBettingData().getTeam2Amount());
		Float rightTeamOdds = GambleUtil.bettingOdds(newResults.getFullBettingData().getTeam2Amount(), newResults.getFullBettingData().getTeam1Amount());
		//store player records first
		if(newResults.getWinningTeam() == newResults.getLeftTeam()) {
			this.repoTransactionManager.reportAsWin(newResults.getBets().getLeft(), leftTeamOdds);
		} else if(newResults.getWinningTeam() == newResults.getRightTeam()){
			this.repoTransactionManager.reportAsLoss(newResults.getBets().getLeft(), leftTeamOdds);
		}
		
		if(newResults.getWinningTeam() == newResults.getLeftTeam()) {
			this.repoTransactionManager.reportAsLoss(newResults.getBets().getRight(), rightTeamOdds);
		} else if(newResults.getWinningTeam() == newResults.getRightTeam()){
			this.repoTransactionManager.reportAsWin(newResults.getBets().getRight(), rightTeamOdds);
		}
		
		//now update the fight stats for the players
		if(newResults.getWinningTeam() == newResults.getLeftTeam()) {
			this.repoTransactionManager.reportAsFightWin(newResults.getTeamData().getLeftTeamData());
		} else if(newResults.getWinningTeam() == newResults.getRightTeam()){
			this.repoTransactionManager.reportAsFightLoss(newResults.getTeamData().getLeftTeamData());
			
		}
		
		if(newResults.getWinningTeam() == newResults.getLeftTeam()) {
			this.repoTransactionManager.reportAsFightLoss(newResults.getTeamData().getRightTeamData());
		} else if(newResults.getWinningTeam() == newResults.getRightTeam()){
			this.repoTransactionManager.reportAsFightWin(newResults.getTeamData().getRightTeamData());
		}
	}
	
	protected void updateBotData(BetResults newResults) {
		Float leftTeamOdds = GambleUtil.bettingOdds(newResults.getFullBettingData().getTeam1Amount(), newResults.getFullBettingData().getTeam2Amount());
		Float rightTeamOdds = GambleUtil.bettingOdds(newResults.getFullBettingData().getTeam2Amount(), newResults.getFullBettingData().getTeam1Amount());
		
		for(BetterBetBot bot: newResults.getBotsWithResults()) {
			if(bot.getResult().getTeam() == newResults.getWinningTeam()) {
				Float winningOdds = newResults.getWinningTeam() == newResults.getLeftTeam() ? leftTeamOdds : rightTeamOdds;
				this.repoTransactionManager.reportBotWin(bot, winningOdds);
			} else {
				this.repoTransactionManager.reportBotLoss(bot);
			}
		}
	}

}
