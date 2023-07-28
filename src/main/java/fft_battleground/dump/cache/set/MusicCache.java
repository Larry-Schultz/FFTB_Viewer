package fft_battleground.dump.cache.set;

import org.springframework.stereotype.Component;

import fft_battleground.dump.cache.DumpCacheSet;
import fft_battleground.music.model.Music;

@Component
public class MusicCache 
extends AbstractDumpCacheSet<Music> 
implements DumpCacheSet<Music> 
{

}
