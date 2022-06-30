package fft_battleground.repo.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fft_battleground.repo.model.SkillBonus;

@Repository
public interface SkillBonusRepo extends JpaRepository<SkillBonus, Long> {
	
	@Query("SELECT skillBonus FROM SkillBonus skillBonus WHERE skillBonus.player = :player")
	public List<SkillBonus> getSkillBonusForPlayer(@Param("player") String player);
	
	@Modifying
	@Query("DELETE FROM SkillBonus skillBonus WHERE skillBonus.player = :player")
	public void deleteSkillForPlayer(@Param("player") String player);
	
	public default void addSkillBonusesForPlayer(String player, Set<String> skillBonuses) {
		for(String skill: skillBonuses) {
			SkillBonus skillBonus = new SkillBonus(player, skill);
			this.save(skillBonus);
		}
		this.flush();
	}
}
