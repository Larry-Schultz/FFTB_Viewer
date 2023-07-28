package fft_battleground.dump.cache.set;

import org.springframework.stereotype.Component;

import fft_battleground.dump.cache.DumpCacheSet;

@Component
public class SoftDeleteCache 
extends AbstractDumpCacheSet<String> 
implements DumpCacheSet<String> {

}
