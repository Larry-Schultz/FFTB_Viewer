package fft_battleground.event.annotate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fft_battleground.event.model.TeamInfoEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.repo.repository.PlayerRecordRepo;
import fft_battleground.tournament.model.Tournament;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TeamInfoEventAnnotator implements BattleGroundEventAnnotator<TeamInfoEvent> {

	@Autowired
	private PlayerRecordRepo playerRecordRepo;
	
	@Getter @Setter 
	private Tournament currentTournament;
	
	@Override
	public void annotateEvent(TeamInfoEvent event) {
		List<PlayerRecord> metadataRecords = new ArrayList<>();
		List<Pair<String, String>> replacementPairList = new ArrayList<>();
		for(Pair<String, String> playerUnitData : event.getPlayerUnitPairs()) {
			PlayerRecord metadata = new PlayerRecord();
			
			//because names from the tournament api have '_' replaced with ' '.  multiple '_' are replaced with a single ' ' 
			String playerName = playerUnitData.getLeft();
			String matchingPlayer;
			if(event.getTeam() == BattleGroundTeam.CHAMPION) {
				matchingPlayer = this.findClosestMatchingName(playerName, currentTournament.getAllPlayers()); //compare to all players for champions
			} else {
				matchingPlayer = this.findClosestMatchingName(playerName, currentTournament.getEntrants());
			}
			Optional<PlayerRecord> record = this.playerRecordRepo.findById(matchingPlayer);
			
			if(record != null && record.isPresent()) {
				metadata.setPlayer(record.get().getPlayer());
				metadata.setFightWins(record.get().getFightWins());
				metadata.setFightLosses(record.get().getFightLosses());
				Pair<String, String> newPair = new ImmutablePair<>(record.get().getPlayer(), playerUnitData.getRight());
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
	
	public String findClosestMatchingName(String playerName, Collection<String> entrants) {
		List<String> cleanedEntrants = Collections.unmodifiableList(entrants.parallelStream().collect(Collectors.toList()));
		LevenshteinDistance distanceCalculator = LevenshteinDistance.getDefaultInstance();
		Map<String, Integer> entrantDistanceMap = new ConcurrentHashMap<>();
		for(String entrant: cleanedEntrants) {
			Integer distance = distanceCalculator.apply(playerName, entrant);
			entrantDistanceMap.put(entrant, distance);
		}
		Optional<String> closestEntrant = entrantDistanceMap.keySet().parallelStream().min(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				Integer distance1 = entrantDistanceMap.get(o1);
				Integer distance2 = entrantDistanceMap.get(o2);
				return distance1.compareTo(distance2);
			}
		});
		
		String result = playerName;
		if(closestEntrant.isPresent()) {
			result = closestEntrant.get();
		}
		
		return result;
	}

}
