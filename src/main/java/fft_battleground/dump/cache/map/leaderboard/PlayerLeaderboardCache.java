package fft_battleground.dump.cache.map.leaderboard;

import org.springframework.stereotype.Component;

import fft_battleground.dump.cache.DumpCacheMap;
import fft_battleground.dump.cache.map.AbstractDumpCacheMap;

@Component
public class PlayerLeaderboardCache 
extends AbstractDumpCacheMap<String, Integer>
implements DumpCacheMap<String, Integer> 
{

}
