package fft_battleground.repo.repository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import fft_battleground.repo.model.PlayerRecord;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface PlayerRecordRepo extends JpaRepository<PlayerRecord, String> {

	@Query("SELECT player.player AS playerName FROM PlayerRecord player_record WHERE player_record.isActive IS NULL OR player_record.isActive = 'Y'") 
    public List<String> findPlayerNames();
	
	@Query("SELECT player_record FROM PlayerRecord player_record") 
    public List<PlayerRecord> findAllPlayerNames();
	
	//SELECT player_record FROM PlayerRecord player WHERE player.player LIKE :username
	@Query("SELECT player_record FROM PlayerRecord player_record WHERE player_record.player LIKE %:username% AND (player_record.isActive IS NULL OR player_record.isActive = 'Y')")
	public List<PlayerRecord> findLikePlayer(String username);
	
	@Query("SELECT player_record.player AS playerName FROM PlayerRecord player_record WHERE player_record.player LIKE %:username% AND (player_record.isActive IS NULL OR player_record.isActive = 'Y')")
	public List<String> findPlayerNameByLike(String username);
	
	@Query("SELECT playerRecord FROM PlayerRecord playerRecord WHERE (playerRecord.isActive IS NULL OR playerRecord.isActive = 'Y') AND (playerRecord.player IN :playerNames)") 
	public List<PersonRecordAllegianceDataProjection> getPlayerDataNeededForAllegianceLeaderboard(@Param("playerNames") Collection<String> playerNames);
	
	@Query(nativeQuery=true, value="SELECT playerRecord.player AS playerName From player_record playerRecord WHERE playerRecord.is_active = 'N'") 
	public List<String> findSoftDeletedPlayers();
	
	@Modifying()
	@Query(nativeQuery=true, value="UPDATE player_record SET is_active = 'N' WHERE player = :player")
	public void softDeletePlayer(@Param("player") String player);
	
	@Modifying()
	@Query(nativeQuery=true, value="UPDATE player_record SET is_active = 'Y' WHERE player = :player")
	public void unDeletePlayer(@Param("player") String player);
	
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
