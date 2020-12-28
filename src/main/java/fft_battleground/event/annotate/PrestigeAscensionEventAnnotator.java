package fft_battleground.event.annotate;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fft_battleground.event.model.PrestigeAscensionEvent;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.repo.repository.PlayerRecordRepo;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PrestigeAscensionEventAnnotator implements BattleGroundEventAnnotator<PrestigeAscensionEvent> {

	@Autowired
	private PlayerRecordRepo playerRecordRepo;
	
	@Override
	public void annotateEvent(PrestigeAscensionEvent event) {
		Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(StringUtils.lowerCase(event.getPrestigeSkillsEvent().getPlayer()));
		if(maybeRecord.isPresent()) {
			event.setCurrentBalance(maybeRecord.get().getLastKnownAmount());
		}
		
		return;
	}

}
