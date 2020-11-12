package fft_battleground.repo;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import fft_battleground.repo.model.PlayerRecord;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface PlayerRecordRepo extends JpaRepository<PlayerRecord, String> {

	@Query("SELECT player.player AS playerName FROM PlayerRecord") 
    public List<String> findPlayerNames();
	
	//SELECT player_record FROM PlayerRecord player WHERE player.player LIKE :username
	@Query("SELECT player_record FROM PlayerRecord player_record WHERE player_record.player LIKE %:username%")
	public List<PlayerRecord> findLikePlayer(String username);
	
	@Query("SELECT player_record.player AS playerName FROM PlayerRecord player_record WHERE player_record.player LIKE %:username%")
	public List<String> findPlayerNameByLike(String username);
	
	@Query("SELECT playerRecord FROM PlayerRecord playerRecord where playerRecord.player IN :playerNames") 
	public List<PersonRecordAllegianceDataProjection> getPlayerDataNeededForAllegianceLeaderboard(@Param("playerNames") Collection<String> playerNames);
	
	@Transactional
	public default List<PlayerRecord> getPlayerDataForAllegiance(Collection<String> playerNames) {
		List<PersonRecordAllegianceDataProjection> playerData = this.getPlayerDataNeededForAllegianceLeaderboard(playerNames);
		List<PlayerRecord> playerRecords = new ArrayList<>();
		for(PersonRecordAllegianceDataProjection projection : playerData) {
			PlayerRecord newRecord = new PlayerRecord();
			newRecord.setPlayer(projection.getPlayer());
			newRecord.setWins(projection.getWins());
			newRecord.setLosses(projection.getLosses());
			newRecord.setFightWins(projection.getFightWins());
			newRecord.setFightLosses(projection.getFightLosses());
			newRecord.setLastKnownLevel(projection.getLastKnownLevel());
			newRecord.setLastKnownAmount(projection.getLastKnownAmount());
			playerRecords.add(newRecord);
		}
		
		return playerRecords;
	}
}

interface PersonRecordAllegianceDataProjection {
	String getPlayer();
	Integer getWins();
	Integer getLosses();
	Integer getFightWins();
	Integer getFightLosses();
	Integer getLastKnownAmount();
	Short getLastKnownLevel();
}
