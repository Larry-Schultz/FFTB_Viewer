package fft_battleground.dump.cache.map;

import java.util.Set;

import org.springframework.stereotype.Component;

import fft_battleground.dump.cache.DumpCacheMap;

@Component
public class SkillBonusCache 
extends AbstractDumpCacheMap<String, Set<String>>
implements DumpCacheMap<String, Set<String>> 
{

}
