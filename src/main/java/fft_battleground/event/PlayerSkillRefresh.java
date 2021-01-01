package fft_battleground.event;

import java.util.List;

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

	public PlayerSkillRefresh(String id, List<String> userSkills, List<String> prestigeSkills, PrestigeAscensionEvent event) {
		this.playerSkillEvent = new PlayerSkillEvent(id, userSkills);
		this.prestigeSkillEvent = new PrestigeSkillsEvent(id, prestigeSkills);
		if(event.getPrestigeSkillsEvent().getSkills() != null) {
			this.prestigeSkillEvent.getSkills().addAll(event.getPrestigeSkillsEvent().getSkills());
		}
	}
}
