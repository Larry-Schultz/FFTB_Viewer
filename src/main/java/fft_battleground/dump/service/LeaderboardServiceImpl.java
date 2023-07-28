package fft_battleground.dump.service;

import java.util.Comparator;
import java.util.Date;
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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import fft_battleground.dump.DumpService;
import fft_battleground.dump.cache.map.BalanceCache;
import fft_battleground.dump.cache.set.BotCache;
import fft_battleground.exception.CacheBuildException;
import fft_battleground.repo.repository.PlayerRecordRepo;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LeaderboardServiceImpl implements LeaderboardService {

	@Autowired
	private DumpService dumpService;
	
	@Autowired
	private PlayerRecordRepo playerRecordRepo;
	
	@Autowired
	private BalanceHistoryService balanceHistoryUtil;
	
	@Autowired
	private BalanceCache balanceCache;
	
	@Autowired
	private BotCache botCache;
	
	@Override
	public Map<String, Integer> getTopPlayers(Integer count) throws CacheBuildException {
		BiMap<String, Integer> topPlayers = HashBiMap.create();
		Map<String, Integer> topPlayerDataMap = this.dumpService.getLeaderboard().keySet().parallelStream()
				.filter(player -> !this.botCache.contains(player))
				.filter(player -> this.playerRecordRepo.findById(StringUtils.lowerCase(player)).isPresent())
				.filter(player -> {
					Date lastActive = this.playerRecordRepo.findById(StringUtils.lowerCase(player)).get()
							.getLastActive();
					boolean result = lastActive != null && this.balanceHistoryUtil.isPlayerActiveInLastMonth(lastActive);
					return result;
				}).collect(Collectors.toMap(Function.identity(),
						player -> this.dumpService.getLeaderboard().get(player)));
		Map.Entry<String, Integer> currentEntry = null;
		try {
			for(Map.Entry<String, Integer> entry : topPlayerDataMap.entrySet()) {
				currentEntry = entry;
				topPlayers.put(entry.getKey(), entry.getValue());
			}
		} catch(IllegalArgumentException e) {
			String errorMessageFormat = "Illegal argument exception populating BiMap.  The current entry is %1$s and %2$o";
			String errorMessage = String.format(errorMessageFormat, currentEntry.getKey(), currentEntry.getValue());
			log.error(errorMessage, e);
			throw new CacheBuildException(errorMessage, e);
		}
		Set<Integer> topValues = topPlayers.values().stream().sorted().limit(count).collect(Collectors.toSet());

		BiMap<Integer, String> topPlayersInverseMap = topPlayers.inverse();
		Map<String, Integer> leaderboardWithoutBots = topValues.stream()
				.collect(Collectors.toMap(rank -> topPlayersInverseMap.get(rank), Function.identity()));
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
