package fft_battleground.repo.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fft_battleground.repo.model.ClassBonus;

@Repository
public interface ClassBonusRepo extends JpaRepository<ClassBonus, Long> {
	
	@Query("SELECT classBonus FROM ClassBonus classBonus WHERE classBonus.player = :player")
	public List<ClassBonus> getClassBonusForPlayer(@Param("player") String player);
	
	@Modifying
	@Query("DELETE FROM ClassBonus classBonus WHERE classBonus.player = :player")
	public void deleteClassBonusForPlayer(@Param("player") String player);
	
	public default void addClassBonusesForPlayer(String player, Set<String> classBonuses) {
		for(String className: classBonuses) {
			ClassBonus classBonus = new ClassBonus(player, className);
			this.save(classBonus);
		}
		this.flush();
	}

}
