package fft_battleground.dump.cache;

import org.springframework.stereotype.Component;

@Component
public class SnubCache 
extends AbstractDumpCacheMap<String, Integer> 
implements DumpCache<String, Integer> 
{

}
