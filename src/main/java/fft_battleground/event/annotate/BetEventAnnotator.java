package fft_battleground.event.annotate;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fft_battleground.event.model.BetEvent;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.repo.repository.PlayerRecordRepo;
import fft_battleground.util.GambleUtil;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BetEventAnnotator implements BattleGroundEventAnnotator<BetEvent> {

	@Autowired
	private PlayerRecordRepo playerRecordRepo;
	
	@Override
	public void annotateEvent(BetEvent event) {
		PlayerRecord metadata = new PlayerRecord();
		Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(StringUtils.lowerCase(event.getPlayer()));
		if(maybeRecord.isPresent()) {
			PlayerRecord record = maybeRecord.get();
			metadata.setPlayer(event.getPlayer());
			metadata.setWins(record.getWins());
			metadata.setLosses(record.getLosses());
			metadata.setLastKnownAmount(record.getLastKnownAmount());
			event.setMetadata(metadata);
			event.setBetAmount(GambleUtil.getBetAmountFromBetString(record, event).toString());
		} else {
			event.setBetAmount(GambleUtil.MINIMUM_BET.toString());
		}
		
		return;
	}

}
