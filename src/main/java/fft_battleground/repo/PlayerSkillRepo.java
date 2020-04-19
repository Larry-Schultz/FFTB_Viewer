package fft_battleground.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fft_battleground.repo.model.PlayerSkills;

public interface PlayerSkillRepo extends JpaRepository<PlayerSkills, Long> {

	@Query("SELECT playerSkills FROM PlayerSkills playerSkills LEFT JOIN playerSkills.player_record playerRecord where playerRecord.player = :player ")
	public List<PlayerSkills> getSkillsByPlayer(@Param("player") String player);
}
