package fft_battleground.event;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fft_battleground.botland.model.DatabaseResultsData;
import fft_battleground.event.model.PlayerSkillEvent;
import fft_battleground.event.model.PrestigeAscensionEvent;
import fft_battleground.event.model.PrestigeSkillsEvent;
import lombok.Data;

@Data
public class PlayerSkillRefresh implements DatabaseResultsData {
	private String player;
	private PlayerSkillEvent playerSkillEvent;
	private PrestigeSkillsEvent prestigeSkillEvent;
	
	public PlayerSkillRefresh() {}
	
	public PlayerSkillRefresh(String player) {
		this.player = player;
	}
	
	public PlayerSkillRefresh(PrestigeAscensionEvent event) {
		this.player = event.getPrestigeSkillsEvent().getPlayer();
		this.prestigeSkillEvent = event.getPrestigeSkillsEvent();
		this.playerSkillEvent = new PlayerSkillEvent(event.getPrestigeSkillsEvent().getPlayer());
	}

	public PlayerSkillRefresh(String player, List<String> userSkills, List<String> prestigeSkills, PrestigeAscensionEvent event) {
		this.player = player;
		this.playerSkillEvent = new PlayerSkillEvent(player, userSkills);
		
		Set<String> skillSet = new HashSet<>();
		skillSet.addAll(prestigeSkills);
		if(event.getPrestigeSkillsEvent().getSkills() != null) {
			skillSet.addAll(event.getPrestigeSkillsEvent().getSkills());
		}
		
		List<String> prestigeSkillsNonDuplicates = new ArrayList<String>(skillSet);
		this.prestigeSkillEvent = new PrestigeSkillsEvent(player, prestigeSkillsNonDuplicates);
		
	}
}
