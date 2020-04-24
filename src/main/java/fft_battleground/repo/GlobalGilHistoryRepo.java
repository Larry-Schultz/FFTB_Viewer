package fft_battleground.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import fft_battleground.repo.model.GlobalGilHistory;

public interface GlobalGilHistoryRepo extends JpaRepository<GlobalGilHistory, String> {

	@Query(nativeQuery=true, value="select sum(amounts.amount) " + 
			"from ( " + 
			"	select sum(left_team_amount) as amount " + 
			"	from real_bet_information " + 
			"	where create_date_time is not null and create_date_time > date_trunc('week', CURRENT_TIMESTAMP - interval '1 week') " + 
			"	union " + 
			"	select sum(right_team_amount) as amount " + 
			"	from real_bet_information " + 
			"	where create_date_time is not null and create_date_time > date_trunc('week', CURRENT_TIMESTAMP - interval '1 week') " + 
			") amounts ")
	public Long getLiquidityFromLastWeek();
	
}
