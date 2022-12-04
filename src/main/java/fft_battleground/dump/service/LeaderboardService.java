package fft_battleground.dump.service;

import java.util.List;
import java.util.Map;

import fft_battleground.exception.CacheBuildException;

public interface LeaderboardService {
	Map<String, Integer> getTopPlayers(Integer count) throws CacheBuildException;
	List<String> getTopActiveBots(Integer count) throws CacheBuildException;
}