package fft_battleground.dump.cache.map;

import org.springframework.stereotype.Component;

import fft_battleground.dump.cache.DumpCacheMap;
import fft_battleground.event.detector.model.ExpEvent;

@Component
public class ExpCache 
extends AbstractDumpCacheMap<String, ExpEvent> 
implements DumpCacheMap<String, ExpEvent> 
{

}
