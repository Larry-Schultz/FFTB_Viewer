package fft_battleground.dump.cache;

import org.springframework.stereotype.Component;

import fft_battleground.model.BattleGroundTeam;

@Component
public class AllegianceCache 
extends AbstractDumpCacheMap<String, BattleGroundTeam>
implements DumpCache<String, BattleGroundTeam> 
{

}
