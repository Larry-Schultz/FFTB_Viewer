package fft_battleground.dump.cache.map;

import java.util.Date;

import org.springframework.stereotype.Component;

import fft_battleground.dump.cache.DumpCacheMap;

@Component
public class LastFightActiveCache 
extends AbstractDumpCacheMap<String, Date> 
implements DumpCacheMap<String, Date> {

}
