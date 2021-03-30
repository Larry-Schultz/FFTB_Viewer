package fft_battleground.repo.repository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import fft_battleground.repo.HitsType;
import fft_battleground.repo.model.Hits;

public interface HitsRepo extends JpaRepository<Hits, Long> {
	
	@Query("")
	public List<Hits> getHitsByDay(String dateString);

	public default String getTodaysDateString() {
		SimpleDateFormat sdf = new SimpleDateFormat(Hits.DISPLAY_FORMAT);
		Date today = new Date();
		String result = sdf.format(today);
		
		return result;
	}
	
	public default Map<HitsType, Hits> getTodaysHits() {
		Map<HitsType, Hits> hitsMap = null;
		String todaysDateString = this.getTodaysDateString();
		List<Hits> todaysHits = this.getHitsByDay(todaysDateString);
		if(todaysHits.size() > 0) {
			hitsMap = todaysHits.stream().collect(Collectors.toMap(Hits::getType, Function.identity()));
		} else {
			hitsMap = new HashMap<>();
			hitsMap.put(HitsType.CRAWLER, new Hits(HitsType.CRAWLER, todaysDateString));
			hitsMap.put(HitsType.USER, new Hits(HitsType.USER, todaysDateString));
		}
		return hitsMap;
	}
}
