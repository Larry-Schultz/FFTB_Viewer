package fft_battleground.repo;

import org.springframework.stereotype.Repository;

import fft_battleground.repo.model.PlayerRecord;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface PlayerRecordRepo extends JpaRepository<PlayerRecord, String> {

	@Query("SELECT player.player AS playerName FROM PlayerRecord") 
    public List<String> findPlayerNames();
	
}
