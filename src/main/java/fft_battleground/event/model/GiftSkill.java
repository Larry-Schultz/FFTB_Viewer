package fft_battleground.event.model;

import fft_battleground.event.detector.model.PlayerSkillEvent;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GiftSkill {
	private String givingPlayer;
	private PlayerSkillEvent playerSkillEvent;
}