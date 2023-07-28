package fft_battleground.dump;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DumpService {
	// Wed Jan 01 00:00:00 EDT 2020
	public static final String dateActiveFormatString = "EEE MMM dd HH:mm:ss z yyyy";
	
	@Getter private Map<String, Integer> leaderboard = new ConcurrentHashMap<>();
	@Getter private Map<Integer, String> expRankLeaderboardByRank = new ConcurrentHashMap<>();
	@Getter private Map<String, Integer> expRankLeaderboardByPlayer = new ConcurrentHashMap<>(); 
	
	public DumpService() {}
	
}
