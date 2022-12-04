package fft_battleground.music;

import java.util.Date;
import java.util.List;
import java.util.Map;

import fft_battleground.repo.model.MusicListenCount;

interface MusicOccurencesCache {
	MusicListenCount getOccurencesbySong(String songName);
	MusicListenCount getOccurencesByClosestSong(String songName);
	void addNewOccurence(List<MusicListenCount> list);
	boolean updateExistingOccurence(String songName, Long additionalOccurences);
	void updateOccurenceCounts();
	Date getFirstOccurence();
	List<MusicListenCount> getOccurences();
	long totalOccurences();
}