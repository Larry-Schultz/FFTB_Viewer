package fft_battleground.repo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import fft_battleground.repo.model.PrestigeSkills;

@Transactional
@Repository
public interface PrestigeSkillsRepo extends JpaRepository<PrestigeSkills, Long> {

	@Query("SELECT prestigeSkills FROM PrestigeSkills prestigeSkills LEFT JOIN prestigeSkills.player_record playerRecord where playerRecord.player = :player ")
	public List<PrestigeSkills> getSkillsByPlayer(@Param("player") String player);
	
	@Query("SELECT playerRecord.player AS player FROM PrestigeSkills prestigeSkills LEFT JOIN prestigeSkills.player_record playerRecord")
	public List<String> getPlayersWithPrestigeSkills();
}
