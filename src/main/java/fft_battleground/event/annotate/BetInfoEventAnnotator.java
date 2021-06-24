package fft_battleground.event.annotate;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fft_battleground.event.detector.model.BetInfoEvent;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.repo.repository.PlayerRecordRepo;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BetInfoEventAnnotator implements BattleGroundEventAnnotator<BetInfoEvent> {

	@Autowired
	private PlayerRecordRepo playerRecordRepo;
	
	@Override
	public void annotateEvent(BetInfoEvent event) {
		PlayerRecord metadata = new PlayerRecord();
		Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(StringUtils.lowerCase(event.getPlayer()));
		if(maybeRecord.isPresent()) {
			PlayerRecord record = maybeRecord.get();
			metadata.setPlayer(event.getPlayer());
			metadata.setWins(record.getWins());
			metadata.setLosses(record.getLosses());
			metadata.setLastKnownAmount(record.getLastKnownAmount());
			event.setMetadata(metadata);
			/*
			 * we set the gold source value for bet info in Botland.addBetInfo, but we get the current data from the database in case its set.
			 * that way the live page has improved bet data for BetInfo bets
			 */
			event.setIsSubscriber(record.isSubscriber());
		}
		
		return;
	}

}
