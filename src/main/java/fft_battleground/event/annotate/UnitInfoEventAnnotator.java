package fft_battleground.event.annotate;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fft_battleground.event.model.UnitInfoEvent;
import fft_battleground.repo.repository.PlayerRecordRepo;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UnitInfoEventAnnotator implements BattleGroundEventAnnotator<UnitInfoEvent> {

	@Autowired
	private PlayerRecordRepo playerRecordRepo;
	
	@Override
	public void annotateEvent(UnitInfoEvent event) {
		String playerName = event.getPlayer();
		playerName = StringUtils.lowerCase(playerName);
		String likePlayerNameString = StringUtils.replace(playerName, " ", "%"); 
		List<String> possiblePlayerNames = this.playerRecordRepo.findPlayerNameByLike(likePlayerNameString);
		if(possiblePlayerNames != null && possiblePlayerNames.size() > 0) {
			event.setPlayer(possiblePlayerNames.get(0));
		} else {
			event.setPlayer(playerName);
		}
	}

}
