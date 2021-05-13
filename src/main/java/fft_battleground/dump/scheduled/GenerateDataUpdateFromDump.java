package fft_battleground.dump.scheduled;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.dump.DumpService;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.fake.ClassBonusEvent;
import fft_battleground.event.model.fake.SkillBonusEvent;
import fft_battleground.exception.DumpException;
import fft_battleground.repo.model.ClassBonus;
import fft_battleground.util.Router;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GenerateDataUpdateFromDump extends TimerTask {

	private Router<BattleGroundEvent> routerRef;
	private DumpService dumpServiceRef;
	
	public GenerateDataUpdateFromDump(Router<BattleGroundEvent> routerRef, DumpService dumpServiceRef) {
		this.routerRef = routerRef;
		this.dumpServiceRef = dumpServiceRef;
	}
	
	@Override
	public void run() {
		log.debug("updating data from dump");

		this.updateBalanceData();
		this.updateExpData();
		this.updateLastActiveData();

		this.updateMusicData();
		
		Collection<String> activePlayers = this.getRecentlyActivePlayers();
		
		this.updateClassBonusCache(activePlayers);
		this.updateSkillBonusCache(activePlayers);
		
		return;
	}
	
	private void updateBalanceData() {
		try {
			Collection<BattleGroundEvent> balanceEvents = this.dumpServiceRef.getBalanceUpdatesFromDumpService();
			balanceEvents.stream().forEach(event -> log.info("Found event from Dump: {} with data: {}", event.getEventType().getEventStringName(), event.toString()));
			this.routerRef.sendAllDataToQueues(balanceEvents);
		} catch(DumpException e) {
			log.error("error getting balance data from dump", e);
			this.dumpServiceRef.getErrorWebhookManager().sendException(e, "error getting balance data from dump");
		}
	}
	
	private void updateExpData() {
		try {
			Collection<BattleGroundEvent> expEvents = this.dumpServiceRef.getExpUpdatesFromDumpService();
			expEvents.stream().forEach(event -> log.info("Found event from Dump: {} with data: {}", event.getEventType().getEventStringName(), event.toString()));
			this.routerRef.sendAllDataToQueues(expEvents);
		} catch(DumpException e) {
			log.error("error getting exp data from dump", e);
			this.dumpServiceRef.getErrorWebhookManager().sendException(e, "error getting exp data from dump");
		}
	}
	
	private void updateLastActiveData() {
		try {
			Collection<BattleGroundEvent> lastActiveEvents = this.dumpServiceRef.getLastActiveUpdatesFromDumpService();
			//lastActiveEvents.stream().forEach(event -> log.info("Found event from Dump: {} with data: {}", event.getEventType().getEventStringName(), event.toString()));
			log.info("Updated {} lastActiveEvents", lastActiveEvents.size());
			this.routerRef.sendAllDataToQueues(lastActiveEvents);
		} catch(DumpException e) {
			log.error("error getting last active data from dump", e);
			this.dumpServiceRef.getErrorWebhookManager().sendException(e, "error getting last active data from dump");
		}
	}
	
	private void updateClassBonusCache(Collection<String> activeFightingPlayers) {
		Set<String> playersWithClassBonus = this.dumpServiceRef.getDumpDataProvider().getPlayersForClassBonusDump()
				.parallelStream().map(playerName -> StringUtils.lowerCase(playerName)).collect(Collectors.toSet()); //lowercase output
		Set<String> activePlayersWithClassBonus = activeFightingPlayers.parallelStream().filter(player -> playersWithClassBonus.contains(player)).collect(Collectors.toSet());
		int count = 0;
		try {
			for(String player: activePlayersWithClassBonus) {
				Set<String> currentClassBonuses = this.dumpServiceRef.getDumpDataProvider().getClassBonus(player);
				currentClassBonuses = ClassBonus.convertToBotOutput(currentClassBonuses);
				this.dumpServiceRef.getClassBonusCache().put(player, currentClassBonuses);
				ClassBonusEvent classBonus = new ClassBonusEvent(player, currentClassBonuses);
				this.routerRef.sendDataToQueues(classBonus);
				count++;
			}
		} catch(DumpException e) {
			log.error("error getting class bonus data from dump", e);
			this.dumpServiceRef.getErrorWebhookManager().sendException(e, "error getting class bonus data from dump");
		}
		log.info("updated class bonus for {} players", count);
	}
	
	private void updateSkillBonusCache(Collection<String> activeFightingPlayers) {
		Set<String> playersWithSkillBonus = this.dumpServiceRef.getDumpDataProvider().getPlayersForSkillBonusDump()
				.parallelStream().map(playerName -> StringUtils.lowerCase(playerName)).collect(Collectors.toSet()); //lowercase output;
		Set<String> activePlayersWithSkillBonus = activeFightingPlayers.parallelStream().filter(player -> playersWithSkillBonus.contains(player)).collect(Collectors.toSet());
		int count = 0;
		try {
			for(String player: activePlayersWithSkillBonus) {
				Set<String> currentSkillBonuses = this.dumpServiceRef.getDumpDataProvider().getSkillBonus(player);
				this.dumpServiceRef.getSkillBonusCache().put(player, currentSkillBonuses);
				SkillBonusEvent skillBonus = new SkillBonusEvent(player, currentSkillBonuses);
				this.routerRef.sendDataToQueues(skillBonus);
				count++;
			}
		} catch(DumpException e) {
			log.error("error getting skill bonus data from dump", e);
			this.dumpServiceRef.getErrorWebhookManager().sendException(e, "error getting skill bonus data from dump");
		}
		log.info("updated skill bonus for {} players", count);
	}
	
	private void updateMusicData() {
		this.dumpServiceRef.setPlaylist();
	}
	
	/**
	 * Get players active in the last 10 minutes.  The goal is to ensure players who are actively
	 * engaged in the stream are getting their data.
	 * @return
	 */
	protected Set<String> getRecentlyActivePlayers() {
		Set<String> activePlayers = new HashSet<>();
		
		Map<String, Date> fightCache = this.dumpServiceRef.getLastFightActiveCache();
		Map<String, Date> betDateCache = this.dumpServiceRef.getLastActiveCache();
		
		List<String> activeFightingPlayers = fightCache.keySet().parallelStream()
				.filter(player -> this.isActiveInLastTenMinutes(fightCache.get(player)))
				.collect(Collectors.toList());
		List<String> activeBetPlayers = betDateCache.keySet().parallelStream()
				.filter(player -> this.isActiveInLastTenMinutes(betDateCache.get(player)))
				.collect(Collectors.toList());
		
		activePlayers.addAll(activeFightingPlayers);
		activePlayers.addAll(activeBetPlayers);
		
		return activePlayers;
	}
	
	protected boolean isActiveInLastTenMinutes(Date lastFightActiveDate) {
		if (lastFightActiveDate == null) {
			return false;
		}

		Calendar tenMinutesAgo = Calendar.getInstance();
		tenMinutesAgo.add(Calendar.MINUTE, -10); // 2020-01-25

		Date tenMinutesAgoDate = tenMinutesAgo.getTime();
		boolean isActive = lastFightActiveDate.after(tenMinutesAgoDate);
		return isActive;
	}
	
}