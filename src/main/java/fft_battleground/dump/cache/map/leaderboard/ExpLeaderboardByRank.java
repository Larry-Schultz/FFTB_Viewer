package fft_battleground.dump.cache.map.leaderboard;

import org.springframework.stereotype.Component;

import fft_battleground.dump.cache.DumpCacheMap;
import fft_battleground.dump.cache.map.AbstractDumpCacheMap;

@Component
public class ExpLeaderboardByRank 
extends AbstractDumpCacheMap<Integer, String>
implements DumpCacheMap<Integer, String> 
{

}
