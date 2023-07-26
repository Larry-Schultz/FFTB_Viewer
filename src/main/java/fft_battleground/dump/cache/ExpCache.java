package fft_battleground.dump.cache;

import org.springframework.stereotype.Component;

import fft_battleground.event.detector.model.ExpEvent;

@Component
public class ExpCache 
extends AbstractDumpCacheMap<String, ExpEvent> 
implements DumpCache<String, ExpEvent> 
{

}
