package fft_battleground.dump.cache.map;

import org.springframework.stereotype.Component;

import fft_battleground.dump.cache.DumpCacheMap;
import fft_battleground.model.BattleGroundTeam;

@Component
public class AllegianceCache 
extends AbstractDumpCacheMap<String, BattleGroundTeam>
implements DumpCacheMap<String, BattleGroundTeam> 
{

}
