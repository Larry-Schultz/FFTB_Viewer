package fft_battleground.music;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import fft_battleground.event.detector.model.MusicEvent;
import fft_battleground.exception.DumpException;
import fft_battleground.music.model.Music;
import fft_battleground.repo.model.MusicListenCount;

public interface MusicService {
	Collection<Music> getPlaylist();
	void addOccurence(MusicEvent event);
	void updatePlaylist() throws DumpException;
	void updateOccurences();
	void freshLoad(Collection<Music> dumpMusicData, List<MusicListenCount> occurrenceData);
	Set<Music> loadMusicDataFromDump() throws DumpException;
	List<MusicListenCount> loadMusicListenCountFromRepo();
}
