package fft_battleground.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import fft_battleground.repo.model.PlayerSkills;

@Transactional
public interface PlayerSkillRepo extends JpaRepository<PlayerSkills, Long> {

	@Query("SELECT playerSkills FROM PlayerSkills playerSkills LEFT JOIN playerSkills.player_record playerRecord where playerRecord.player = :player ")
	public List<PlayerSkills> getSkillsByPlayer(@Param("player") String player);
	
	@Modifying
	@Query(" DELETE FROM PlayerSkills playerSkills "
		 + " WHERE playerSkills IN "
		 + " ("
		 + " SELECT playerSkills "
		 + " FROM PlayerSkills playerSkills LEFT JOIN playerSkills.player_record playerRecord "
		 + " WHERE playerRecord.player = :player"
		 + " ) ")
	public void deleteSkillsByPlayer(@Param("player") String player);
	
	@Query("Select playerSkills FROM PlayerSkills playerSkills LEFT JOIN playerSkills.player_record playerRecord WHERE playerRecord.player = :player AND playerSkills.skill = :skillName")
	public PlayerSkills getSkillsByPlayerAndSkillName(@Param("player") String player, @Param("skillName") String skillName);
	
	@Query("SELECT COUNT(playerSkills) FROM PlayerSkills playerSkills LEFT JOIN playerSkills.player_record playerRecord WHERE playerSkills.skillType = 'PRESTIGE' AND playerRecord.player IN :players")
	public Integer getPrestigeSkillsCount(@Param("players") List<String> players);
}
