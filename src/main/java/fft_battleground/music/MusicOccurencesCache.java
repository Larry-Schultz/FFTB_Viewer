package fft_battleground.music;

import java.util.Date;
import java.util.List;
import java.util.Map;

import fft_battleground.repo.model.MusicListenCount;

interface MusicOccurencesCache {
	MusicListenCount getOccurencesById(Long id);
	MusicListenCount getOccurencesbySong(String songName);
	void addNewOccurence(List<MusicListenCount> list);
	boolean updateExistingOccurence(Long songId, Long additionalOccurences);
	void updateOccurenceCounts();
	Map<Long, MusicListenCount> getOccurencesIdView();
	Date getFirstOccurence();
	long getTotalOccurences();
	long songCountWithOccurences();
}