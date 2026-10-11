package fft_battleground.dump.service;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import fft_battleground.dump.cache.map.BalanceCache;
import fft_battleground.dump.cache.map.leaderboard.PlayerLeaderboardCache;
import fft_battleground.dump.cache.set.BotCache;
import fft_battleground.exception.CacheBuildException;
import fft_battleground.repo.repository.PlayerRecordRepo;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LeaderboardServiceImpl implements LeaderboardService {
	
	@Autowired
	private PlayerRecordRepo playerRecordRepo;
	
	@Autowired
	private BalanceHistoryService balanceHistoryUtil;
	
	@Autowired
	private BalanceCache balanceCache;
	
	@Autowired
	private BotCache botCache;
	
	@Autowired
	private PlayerLeaderboardCache playerLeaderboardCache;
	
	@Override
	public Map<String, Integer> getTopPlayers(Integer count) throws CacheBuildException {
		Map<String, Integer> playerLeaderboardMap = this.playerLeaderboardCache.getMap();
		Map<String, Integer> topPlayerDataMap = playerLeaderboardMap.keySet().parallelStream()
				.filter(player -> !this.botCache.contains(player))
				.filter(player -> this.playerRecordRepo.findById(StringUtils.lowerCase(player)).isPresent())
				.filter(player -> {
					Date lastActive = this.playerRecordRepo.findById(StringUtils.lowerCase(player)).get()
							.getLastActive();
					boolean result = lastActive != null && this.balanceHistoryUtil.isPlayerActiveInLastMonth(lastActive);
					return result;
				}).collect(Collectors.toMap(Function.identity(),
						player -> playerLeaderboardMap.get(player)));
		//dump positions should be unique per player, but duplicates occur (parse glitches, duplicate dump lines).
		//A BiMap inverse crashes the whole leaderboard build on any tie; rank first-wins instead and keep building.
		Map<Integer, String> rankToPlayer = new HashMap<>();
		for(Map.Entry<String, Integer> entry : topPlayerDataMap.entrySet()) {
			String previous = rankToPlayer.putIfAbsent(entry.getValue(), entry.getKey());
			if(previous != null) {
				log.warn("Duplicate leaderboard position {}: keeping {} over {}", entry.getValue(), previous, entry.getKey());
			}
		}
		List<Integer> topValues = rankToPlayer.keySet().stream().sorted().limit(count).collect(Collectors.toList());

		Map<String, Integer> leaderboardWithoutBots = topValues.stream()
				.collect(Collectors.toMap(rank -> rankToPlayer.get(rank), Function.identity()));
		return leaderboardWithoutBots;
	}

	@Override
	public List<String> getTopActiveBots(Integer count) throws CacheBuildException {
		Set<String> botCacheSet = this.botCache.getSet();
		List<String> topActiveBots = botCacheSet.parallelStream()
				.filter(player -> this.balanceHistoryUtil.isPlayerActiveInLastMonth(player))
				.map(player -> new ImmutablePair<String, Integer>(player, this.balanceCache.get(player)))
				.sorted(Comparator.comparing(Pair<String, Integer>::getRight))
				.limit(count).map(Pair<String, Integer>::getLeft)
				.collect(Collectors.toList());
		
		return topActiveBots;
	}
}
