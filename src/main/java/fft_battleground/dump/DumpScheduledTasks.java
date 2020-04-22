package fft_battleground.dump;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.google.common.collect.MapDifference.ValueDifference;

import fft_battleground.event.model.AllegianceEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.PlayerSkillEvent;
import fft_battleground.event.model.PortraitEvent;
import fft_battleground.event.model.PrestigeSkillsEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.repo.PlayerRecordRepo;
import fft_battleground.repo.PlayerSkillRepo;
import fft_battleground.repo.model.PlayerSkills;
import fft_battleground.util.Router;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DumpScheduledTasks {

	@Autowired
	private Router<BattleGroundEvent> eventRouter;
	
	@Autowired
	private DumpService dumpService;
	
	@Autowired
	private DumpResourceManager dumpResourceManager;
	
	@Autowired
	private PlayerRecordRepo playerRecordRepo;
	
	@Autowired
	private PlayerSkillRepo playerSkillRepo;
	
	@Scheduled(cron = "0 0 1 * * ?")
	public Map<String, String> updatePortraits() {
		log.info("updating portrait cache");
		Set<String> playerNamesSet = this.dumpService.getPortraitCache().keySet();
		Map<String, String> portraitsFromDump = playerNamesSet.parallelStream().collect(Collectors.toMap(Function.identity(), player -> this.dumpResourceManager.getPortraitForPlayer(player)));
		
		Map<String, ValueDifference<String>> balanceDelta = Maps.difference(this.dumpService.getPortraitCache(), portraitsFromDump).entriesDiffering();
		List<BattleGroundEvent> portraitEvents = new LinkedList<>();
		for(String key: balanceDelta.keySet()) {
			PortraitEvent event = new PortraitEvent(key, balanceDelta.get(key).rightValue());
			portraitEvents.add(event);
			//update cache with new data
			this.dumpService.getPortraitCache().put(key, balanceDelta.get(key).rightValue());
		}
		
		portraitEvents.stream().forEach(event -> log.info("Found event from Dump: {} with data: {}", event.getEventType().getEventStringName(), event.toString()));
		this.eventRouter.sendAllDataToQueues(portraitEvents);
		log.info("portrait cache update complete");
		
		return portraitsFromDump;
	}
	
	@Scheduled(cron = "0 15 1 * * ?")
	public Map<String, BattleGroundTeam> updateAllegiances() {
		log.info("updating allegiances cache");
		Set<String> playerNamesSet = this.dumpService.getAllegianceCache().keySet();
		Map<String, BattleGroundTeam> allegiancesFromDump = playerNamesSet.parallelStream().collect(Collectors.toMap(Function.identity(), player -> this.dumpResourceManager.getAllegianceForPlayer(player)));
		
		Map<String, ValueDifference<BattleGroundTeam>> balanceDelta = Maps.difference(this.dumpService.getAllegianceCache(), allegiancesFromDump).entriesDiffering();
		List<BattleGroundEvent> allegianceEvents = new LinkedList<>();
		for(String key: balanceDelta.keySet()) {
			AllegianceEvent event = new AllegianceEvent(key, balanceDelta.get(key).rightValue());
			allegianceEvents.add(event);
			//update cache with new data
			this.dumpService.getAllegianceCache().put(key, balanceDelta.get(key).rightValue());
		}
		
		allegianceEvents.stream().forEach(event -> log.info("Found event from Dump: {} with data: {}", event.getEventType().getEventStringName(), event.toString()));
		this.eventRouter.sendAllDataToQueues(allegianceEvents);
		log.info("allegiances cache update complete.");
		
		return allegiancesFromDump;
	}
	
	@Scheduled(cron = "0 30 1 * * ?")
	public void updateAllSkills() {
		this.updatePrestigeSkills();
		this.updateUserSkills();
	}
	
	public Map<String, List<String>> updateUserSkills() {
		log.info("updating user skills cache");
		Set<String> playerNamesSet = this.dumpService.getUserSkillsCache().keySet();
		Map<String, List<String>> userSkillsFromDump = playerNamesSet.parallelStream().collect(Collectors.toMap(Function.identity(), player -> this.dumpResourceManager.getSkillsForPlayer(player)));
		
		Map<String, List<String>> differences = this.dumpService.getUserSkillsCache().keySet().parallelStream().collect(Collectors.toMap(Function.identity(), 
													key -> ListUtils.<String>subtract(userSkillsFromDump.get(key), this.dumpService.getUserSkillsCache().get(key))));
		List<BattleGroundEvent> skillEvents = new ArrayList<>();
		for(String key : differences.keySet()) {
			if(differences.get(key).size() > 0) {
				skillEvents.add(new PlayerSkillEvent(key, differences.get(key)));
			}
		}
		
		skillEvents.stream().forEach(event -> log.info("Found event from Dump: {} with data: {}", event.getEventType().getEventStringName(), event.toString()));
		this.eventRouter.sendAllDataToQueues(skillEvents);
		log.info("user skills cache update complete");
		
		return userSkillsFromDump;
	}
	
	public Map<String, List<String>> updatePrestigeSkills() {
		log.info("updating prestige skills cache");
		Set<String> playerNamesSet = this.dumpService.getUserSkillsCache().keySet(); //use the larger set of names from the user skills cache
		Map<String, List<String>> prestigeSkillsFromDump = playerNamesSet.parallelStream().collect(Collectors.toMap(Function.identity(), player -> this.dumpResourceManager.getPrestigeSkillsForPlayer(player)));
		
		Map<String, List<String>> differences = this.dumpService.getPrestigeSkillsCache().keySet().parallelStream().collect(Collectors.toMap(Function.identity(), 
													key -> ListUtils.<String>subtract(prestigeSkillsFromDump.get(key), this.dumpService.getPrestigeSkillsCache().get(key))));
		List<BattleGroundEvent> skillEvents = new ArrayList<>();
		Set<String> playersWithNewPrestige = new TreeSet<>();
		for(String key : differences.keySet()) {
			if(differences.get(key).size() > 0) {
				skillEvents.add(new PrestigeSkillsEvent(key, differences.get(key)));
				playersWithNewPrestige.add(key);
			}
		}
		
		skillEvents.stream().forEach(event -> log.info("Found event from Dump: {} with data: {}", event.getEventType().getEventStringName(), event.toString()));
		playersWithNewPrestige.forEach(player -> {
			List<PlayerSkills> playerSkills = this.playerSkillRepo.getSkillsByPlayer(player);
			playerSkills.parallelStream().forEach(skill -> this.playerSkillRepo.delete(skill));
			log.info("deleting skills for {}", player);
		});
		this.eventRouter.sendAllDataToQueues(skillEvents);
		log.info("prestige skills cache update complete");
		
		return prestigeSkillsFromDump;
	}
	
	@Scheduled(cron = "0 0 2 * * ?")
	public Set<String> updateBotList() {
		log.info("updating bot list");
		
		Set<String> dumpBots = this.dumpResourceManager.getBots();
		dumpBots.stream().forEach(botName -> this.dumpService.getBotCache().add(botName));
		
		log.info("bot list update complete");
		return this.dumpService.getBotCache();
	}
}
