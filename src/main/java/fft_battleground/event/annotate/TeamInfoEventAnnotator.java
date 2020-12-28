package fft_battleground.event.annotate;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fft_battleground.event.model.TeamInfoEvent;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.repo.repository.PlayerRecordRepo;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TeamInfoEventAnnotator implements BattleGroundEventAnnotator<TeamInfoEvent> {

	@Autowired
	private PlayerRecordRepo playerRecordRepo;
	
	@Override
	public void annotateEvent(TeamInfoEvent event) {
		List<PlayerRecord> metadataRecords = new ArrayList<>();
		List<Pair<String, String>> replacementPairList = new ArrayList<>();
		for(Pair<String, String> playerUnitData : event.getPlayerUnitPairs()) {
			PlayerRecord metadata = new PlayerRecord();
			
			//because names from the tournament api have '_' replaced with ' '.  multiple '_' are replaced with a single ' ' 
			String playerName = playerUnitData.getLeft();
			playerName = StringUtils.lowerCase(playerName);
			String likePlayerNameString = StringUtils.replace(playerName, " ", "%"); 
			List<PlayerRecord> records = this.playerRecordRepo.findLikePlayer(likePlayerNameString);
			
			if(records != null && records.size() > 0) {
				PlayerRecord record = records.get(0);
				metadata.setPlayer(record.getPlayer());
				metadata.setFightWins(record.getFightWins());
				metadata.setFightLosses(record.getFightLosses());
				Pair<String, String> newPair = new ImmutablePair<>(record.getPlayer(), playerUnitData.getRight());
				replacementPairList.add(newPair);
			} else {
				metadata.setPlayer(playerUnitData.getLeft());
				metadata.setFightWins(0);
				metadata.setFightLosses(0);
				replacementPairList.add(playerUnitData);
			}
			metadataRecords.add(metadata);
		}
		event.setPlayerUnitPairs(replacementPairList);
		
		event.setMetaData(metadataRecords);
	}

}
