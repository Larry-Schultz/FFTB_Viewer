package fft_battleground.music;

import java.util.Collection;

import fft_battleground.music.model.Music;

public interface MusicSongNameMap {
	Long getMusicIdBySong(String song);
	void refreshCache(Collection<Music> dumpMusicData);
}