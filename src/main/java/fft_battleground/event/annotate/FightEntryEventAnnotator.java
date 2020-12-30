package fft_battleground.event.annotate;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fft_battleground.event.model.FightEntryEvent;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.repo.repository.PlayerRecordRepo;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FightEntryEventAnnotator implements BattleGroundEventAnnotator<FightEntryEvent> {

	@Autowired
	private PlayerRecordRepo playerRecordRepo;
	
	@Override
	public void annotateEvent(FightEntryEvent event) {
		PlayerRecord metadata = new PlayerRecord();
		Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(StringUtils.lowerCase(event.getPlayer()));
		if(maybeRecord.isPresent()) {
			PlayerRecord record = maybeRecord.get();
			metadata.setPlayer(event.getPlayer());
			metadata.setFightWins(record.getFightWins());
			metadata.setFightLosses(record.getFightLosses());
			metadata.setAllegiance(record.getAllegiance());
			event.setMetadata(metadata);
		}
		
		return;
	}

}
