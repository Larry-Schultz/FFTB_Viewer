package fft_battleground.dump.scheduled;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fft_battleground.dump.DumpService;
import fft_battleground.event.detector.model.PlayerSkillEvent;
import fft_battleground.event.detector.model.fake.ClassBonusEvent;
import fft_battleground.event.detector.model.fake.SkillBonusEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.exception.DumpException;
import fft_battleground.repo.model.ClassBonus;
import fft_battleground.repo.model.PlayerSkills;
import fft_battleground.util.Router;

public class GenerateDataUpdateFromDump extends TimerTask {
	Logger log = LoggerFactory.getLogger("dataUpdate");
	
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
		this.updateSnubData();

		this.updateMusicData();
		
		this.updateClassBonusCache();
		this.updateSkillBonusCache();
		this.updateUserSkills();
		this.updatePrestigeSkills();
		
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
	
	private void updateSnubData() {
		try {
			Collection<BattleGroundEvent> snubEvents = this.dumpServiceRef.getSnubUpdatesFromDumpService();
			log.info("Updated {} snub events", snubEvents.size());
			this.routerRef.sendAllDataToQueues(snubEvents);
		} catch(DumpException e) {
			log.error("error getting snub data from dump", e);
			this.dumpServiceRef.getErrorWebhookManager().sendException(e, "error getting snub data from dump");
		}
	}
	
	private void updateClassBonusCache() {
		Set<String> playersWithUpdatedClassBonus = this.dumpServiceRef.getDumpDataProvider().getRecentPlayersForClassBonusDump()
				.parallelStream().map(playerName -> StringUtils.lowerCase(playerName)).collect(Collectors.toSet()); //lowercase output
		AtomicInteger count = new AtomicInteger(0);
		playersWithUpdatedClassBonus.parallelStream().forEach(player -> {
			try {
				Set<String> currentClassBonuses = this.dumpServiceRef.getDumpDataProvider().getClassBonus(player);
				currentClassBonuses = ClassBonus.convertToBotOutput(currentClassBonuses);
				this.dumpServiceRef.getClassBonusCache().put(player, currentClassBonuses);
				ClassBonusEvent classBonus = new ClassBonusEvent(player, currentClassBonuses);
				this.routerRef.sendDataToQueues(classBonus);
				count.getAndIncrement();
			} catch(DumpException e) {
				log.error("error getting class bonus data from dump", e);
				this.dumpServiceRef.getErrorWebhookManager().sendException(e, "error getting class bonus data from dump");
			}
		});
		this.logPlayers("class bonuses", count.get(), playersWithUpdatedClassBonus);
	}
	
	private void updateSkillBonusCache() {
		Set<String> playersWithUpdatedSkillBonus = this.dumpServiceRef.getDumpDataProvider().getRecentPlayersForSkillBonusDump()
				.parallelStream().map(playerName -> StringUtils.lowerCase(playerName)).collect(Collectors.toSet()); //lowercase output;
		AtomicInteger count = new AtomicInteger(0);
		playersWithUpdatedSkillBonus.parallelStream().forEach(player -> {
			try {
				Set<String> currentSkillBonuses = this.dumpServiceRef.getDumpDataProvider().getSkillBonus(player);
				this.dumpServiceRef.getSkillBonusCache().put(player, currentSkillBonuses);
				SkillBonusEvent skillBonus = new SkillBonusEvent(player, currentSkillBonuses);
				this.routerRef.sendDataToQueues(skillBonus);
				count.getAndIncrement();
			} catch(Exception e) {
				log.error("error getting skill bonus data from dump", e);
				this.dumpServiceRef.getErrorWebhookManager().sendException(e, "error getting skill bonus data from dump");
			}
		});
		this.logPlayers("skill bonuses", count.get(), playersWithUpdatedSkillBonus);
	}
	
	private void updateUserSkills() {
		Set<String> playersWithUpdatedUserSkills = this.dumpServiceRef.getDumpDataProvider().getRecentPlayersForUserSkillsDump()
				.parallelStream().map(playerName -> StringUtils.lowerCase(playerName)).collect(Collectors.toSet()); //lowercase output;
		AtomicInteger count = new AtomicInteger(0);
		playersWithUpdatedUserSkills.parallelStream().forEach(player -> {
			try {
				List<PlayerSkills> currentUserSkills = this.dumpServiceRef.getDumpDataProvider().getSkillsForPlayer(player);
				this.dumpServiceRef.getMonsterUtils().categorizeSkillsList(currentUserSkills);
				this.dumpServiceRef.getMonsterUtils().regulateMonsterSkillCooldowns(currentUserSkills);
				List<String> skills = PlayerSkills.convertToListOfSkillStrings(currentUserSkills);
				this.dumpServiceRef.getUserSkillsCache().put(player, skills);
				PlayerSkillEvent playerSkillEvent = new PlayerSkillEvent(currentUserSkills, player);
				this.routerRef.sendDataToQueues(playerSkillEvent);
				count.getAndIncrement();
			} catch (Exception e) {
				log.error("error getting user skill data from dump", e);
				this.dumpServiceRef.getErrorWebhookManager().sendException(e, "error getting user skill data from dump");
			}
		});
		this.logPlayers("user skills", count.get(), playersWithUpdatedUserSkills);
	}
	
	private void updatePrestigeSkills() {
		
	}
	
	private void updateMusicData() {
		this.dumpServiceRef.setPlaylist();
	}
	
	protected void logPlayers(String logType, int count, Collection<String> players) {
		List<String> playerNamesList = new ArrayList<>(players);
		Collections.sort(playerNamesList);
		String playerNames = StringUtils.join(playerNamesList.toArray(new String[] {}),", ");
		log.info("updated {} for {} players.  The players: {}", logType, count, playerNames) ;
	}
	
}