package fft_battleground.event.annotate;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fft_battleground.dump.cache.set.BotCache;
import fft_battleground.event.detector.model.BetEvent;
import fft_battleground.exception.NotANumberBetException;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.repo.repository.PlayerRecordRepo;
import fft_battleground.util.GambleUtil;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BetEventAnnotator implements BattleGroundEventAnnotator<BetEvent> {

	@Autowired
	private PlayerRecordRepo playerRecordRepo;
	
	@Autowired
	private BotCache botCache;
	
	@Override
	public void annotateEvent(BetEvent event) throws NotANumberBetException {
		this.annotePlayerMetadata(event);
		String cleanedPlayerName = GambleUtil.cleanString(event.getPlayer());
		if(this.botCache.contains(cleanedPlayerName)) {
			if(event.getBetAmountInteger() != event.getMetadata().getLastKnownAmount()) {
				Integer botBet = Math.min(GambleUtil.MAX_BET, event.getBetAmountInteger());
				event.setBetAmount(botBet.toString());
			}
		}
		return;
	}
	
	private void annotePlayerMetadata(BetEvent event) throws NotANumberBetException {
		PlayerRecord metadata = new PlayerRecord();
		Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(StringUtils.lowerCase(event.getPlayer()));
		if(maybeRecord.isPresent()) {
			PlayerRecord record = maybeRecord.get();
			metadata.setPlayer(event.getPlayer());
			metadata.setWins(record.getWins());
			metadata.setLosses(record.getLosses());
			metadata.setLastKnownAmount(record.getLastKnownAmount());
			metadata.setIsSubscriber(record.isSubscriber());
			event.setMetadata(metadata);
			event.setBetAmount(GambleUtil.getBetAmountFromBetString(record, event).toString());
		} else {
			event.setBetAmount(GambleUtil.MINIMUM_BET.toString());
		}
		
	}

}
