package fft_battleground.dump;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.google.common.collect.MapDifference.ValueDifference;

import fft_battleground.botland.model.DatabaseResultsData;
import fft_battleground.event.PlayerSkillRefresh;
import fft_battleground.event.model.AllegianceEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.PlayerSkillEvent;
import fft_battleground.event.model.PortraitEvent;
import fft_battleground.event.model.PrestigeSkillsEvent;
import fft_battleground.model.BattleGroundTeam;
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
	private DumpDataProvider dumpDataProvider;
	
	@Autowired
	private Router<DatabaseResultsData> betResultsRouter;
	
	Timer timer = new Timer();
	
	@Scheduled(cron = "0 0 1 * * ?")
	public void runAllUpdates() {
		DumpScheduledTask[] dumpScheduledTasks = new DumpScheduledTask[] {
				new AllegianceTask(this), 
				new BotListTask(this), 
				new PortraitsTask(this),
				new UserSkillsTask(this)
			};
		for(DumpScheduledTask task : dumpScheduledTasks) {
			this.timer.schedule(task, 0);
		}
	}
	
	public Map<String, String> updatePortraits() {
		log.info("updating portrait cache");
		Set<String> playerNamesSet = this.dumpDataProvider.getPlayersForPortraitDump();
		Map<String, String> portraitsFromDump = new HashMap<>();
		for(String player: playerNamesSet) {
			String portrait = this.dumpDataProvider.getPortraitForPlayer(player);
			if(!StringUtils.isBlank(portrait)) {
				portraitsFromDump.put(player, portrait);
			}
		}
		
		Map<String, ValueDifference<String>> balanceDelta = Maps.difference(this.dumpService.getPortraitCache(), portraitsFromDump).entriesDiffering();
		List<BattleGroundEvent> portraitEvents = new LinkedList<>();
		//find differences
		for(String key: balanceDelta.keySet()) {
			PortraitEvent event = new PortraitEvent(key, balanceDelta.get(key).rightValue());
			portraitEvents.add(event);
			//update cache with new data
			this.dumpService.getPortraitCache().put(key, balanceDelta.get(key).rightValue());
		}
		
		//add missing players
		for(String key: portraitsFromDump.keySet()) {
			if(!this.dumpService.getPortraitCache().containsKey(key)) {
				PortraitEvent event = new PortraitEvent(key, portraitsFromDump.get(key));
				portraitEvents.add(event);
				this.dumpService.getPortraitCache().put(key, portraitsFromDump.get(key));
			}
		}
		
		portraitEvents.stream().forEach(event -> log.info("Found event from Dump: {} with data: {}", event.getEventType().getEventStringName(), event.toString()));
		this.eventRouter.sendAllDataToQueues(portraitEvents);
		log.info("portrait cache update complete");
		
		return portraitsFromDump;
	}
	
	public Map<String, BattleGroundTeam> updateAllegiances() {
		log.info("updating allegiances cache");
		Set<String> playerNamesSet = this.dumpDataProvider.getPlayersForAllegianceDump();
		Map<String, BattleGroundTeam> allegiancesFromDump = new HashMap<>();
		for(String player : playerNamesSet) {
			BattleGroundTeam team = this.dumpDataProvider.getAllegianceForPlayer(player);
			if(team != null) {
				allegiancesFromDump.put(player, team);
			}
		}
		
		Map<String, ValueDifference<BattleGroundTeam>> balanceDelta = Maps.difference(this.dumpService.getAllegianceCache(), allegiancesFromDump).entriesDiffering();
		List<BattleGroundEvent> allegianceEvents = new LinkedList<>();
		//find differences
		for(String key: balanceDelta.keySet()) {
			AllegianceEvent event = new AllegianceEvent(key, balanceDelta.get(key).rightValue());
			allegianceEvents.add(event);
			//update cache with new data
			this.dumpService.getAllegianceCache().put(key, balanceDelta.get(key).rightValue());
		}
		
		//add missing players
		for(String key: allegiancesFromDump.keySet()) {
			if(!this.dumpService.getAllegianceCache().containsKey(key)) {
				AllegianceEvent event = new AllegianceEvent(key, allegiancesFromDump.get(key));
				allegianceEvents.add(event);
				this.dumpService.getAllegianceCache().put(key, allegiancesFromDump.get(key));
			}
		}
		
		allegianceEvents.stream().forEach(event -> log.info("Found event from Dump: {} with data: {}", event.getEventType().getEventStringName(), event.toString()));
		this.eventRouter.sendAllDataToQueues(allegianceEvents);
		log.info("allegiances cache update complete.");
		
		return allegiancesFromDump;
	}
	
	public void updateAllSkills() {
		//this.updateUserSkills();
		//this.updatePrestigeSkills();
		
		log.info("updating user and prestige skills caches");
		Set<String> userSkillPlayers = this.dumpDataProvider.getPlayersForUserSkillsDump(); //use the larger set of names from the leaderboard
		Set<String> prestigeSkillPlayers = this.dumpDataProvider.getPlayersForPrestigeSkillsDump(); //use the larger set of names from the leaderboard
		
		//assume all players with prestige skills have user skills
		for(String player: userSkillPlayers) {
			PlayerSkillRefresh refresh = new PlayerSkillRefresh(player);
			//delete all skills from cache
			this.dumpService.getUserSkillsCache().remove(player);
			
			//get user skills
			List<String> userSkills = this.dumpDataProvider.getSkillsForPlayer(player);
			
			//store user skills
			this.dumpService.getUserSkillsCache().put(player, userSkills);
			PlayerSkillEvent userSkillsEvent = new PlayerSkillEvent(player, userSkills);
			refresh.setPlayerSkillEvent(userSkillsEvent);
			
			if(prestigeSkillPlayers.contains(player)) {
				//get prestige skills
				List<String> prestigeSkills = this.dumpDataProvider.getPrestigeSkillsForPlayer(player);
				
				//store prestige skills
				this.dumpService.getPrestigeSkillsCache().remove(player);
				this.dumpService.getPrestigeSkillsCache().put(player, prestigeSkills);
				PrestigeSkillsEvent prestigeEvent = new PrestigeSkillsEvent(player, prestigeSkills);
				refresh.setPrestigeSkillEvent(prestigeEvent);
			}
			this.betResultsRouter.sendDataToQueues(refresh);
			log.info("refreshed skills for player: {}", player);
			
		}
		log.info("user and prestige skill cache updates complete");
	}
	
	public Set<String> updateBotList() {
		log.info("updating bot list");
		
		Set<String> dumpBots = this.dumpDataProvider.getBots();
		dumpBots.stream().forEach(botName -> this.dumpService.getBotCache().add(botName));
		
		log.info("bot list update complete");
		return this.dumpService.getBotCache();
	}
	
}

abstract class DumpScheduledTask extends TimerTask {
	protected DumpScheduledTasks dumpScheduledTasksRef;
	
	public DumpScheduledTask(DumpScheduledTasks dumpScheduledTasks) {
		this.dumpScheduledTasksRef = dumpScheduledTasks;
	}
	
	public void run() {
		this.task();
	}
	
	protected abstract void task();
}

class AllegianceTask extends DumpScheduledTask {
	public AllegianceTask(DumpScheduledTasks dumpScheduledTasks) { super(dumpScheduledTasks);}
	protected void task() {this.dumpScheduledTasksRef.updateAllegiances();}
}

class UserSkillsTask extends DumpScheduledTask {
	public UserSkillsTask(DumpScheduledTasks dumpScheduledTasks) {super(dumpScheduledTasks);}
	protected void task() {this.dumpScheduledTasksRef.updateAllSkills();}
}

class BotListTask extends DumpScheduledTask {
	public BotListTask(DumpScheduledTasks dumpScheduledTasks) {super(dumpScheduledTasks);}
	protected void task() {this.dumpScheduledTasksRef.updateBotList();}
}

class PortraitsTask extends DumpScheduledTask {
	public PortraitsTask(DumpScheduledTasks dumpScheduledTasks) {super(dumpScheduledTasks);}
	protected void task() {this.dumpScheduledTasksRef.updatePortraits();}
}
