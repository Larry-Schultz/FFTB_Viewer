package fft_battleground.repo.repository;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fft_battleground.repo.model.BalanceHistory;
import lombok.SneakyThrows;

public interface BalanceHistoryRepo extends JpaRepository<BalanceHistory, String> {

	@Query("Select balanceHistory from BalanceHistory balanceHistory where balanceHistory.player = :player AND balanceHistory.type= 'DUMP' ORDER BY create_timestamp DESC")
	public List<BalanceHistory> getTournamentBalanceHistory(String player, Pageable pageable);
	
	@Query("Select balanceHistory from BalanceHistory balanceHistory where balanceHistory.player = :player AND balanceHistory.type= 'DUMP' AND balanceHistory.create_timestamp >= :timestamp ORDER BY create_timestamp DESC")
	public List<BalanceHistory> getTournamentBalanceHistory(@Param("player") String player, @Param("timestamp") Date timestamp);
	
	@SneakyThrows
	public default List<BalanceHistory> getTournamentBalanceHistoryFromPastWeek(String player) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -7);
		Date timestamp = calendar.getTime();
		List<BalanceHistory> data = this.getTournamentBalanceHistory(player, timestamp);
		SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh");
		return data;
	}
}
