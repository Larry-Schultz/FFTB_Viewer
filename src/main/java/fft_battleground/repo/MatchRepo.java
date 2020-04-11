package fft_battleground.repo;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fft_battleground.repo.model.Match;
import fft_battleground.repo.model.TeamInfo;

@Repository
public interface MatchRepo extends JpaRepository<Match, Long> {
	
	@Query("Select teamInfo from TeamInfo teamInfo where teamInfo.player = :player ORDER BY createDateTime DESC")
	public List<TeamInfo> getLatestTeamInfoForPlayer(String player, Pageable pageable);
}
