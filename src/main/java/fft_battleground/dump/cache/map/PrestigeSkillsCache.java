package fft_battleground.dump.cache.map;

import java.util.List;

import org.springframework.stereotype.Component;

import fft_battleground.dump.cache.DumpCacheMap;

@Component
public class PrestigeSkillsCache 
extends AbstractDumpCacheMap<String, List<String>>
implements DumpCacheMap<String, List<String>> 
{

}
