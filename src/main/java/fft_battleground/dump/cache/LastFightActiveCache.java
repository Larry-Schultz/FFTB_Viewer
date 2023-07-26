package fft_battleground.dump.cache;

import java.util.Date;

import org.springframework.stereotype.Component;

@Component
public class LastFightActiveCache 
extends AbstractDumpCacheMap<String, Date> 
implements DumpCache<String, Date> {

}
