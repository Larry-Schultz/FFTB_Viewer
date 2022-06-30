package fft_battleground.music;

import java.io.IOException;
import java.io.StringReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import fft_battleground.dump.DumpDataProvider;
import fft_battleground.event.detector.model.MusicEvent;
import fft_battleground.exception.DumpException;
import fft_battleground.music.model.Music;
import fft_battleground.repo.model.MusicListenCount;
import fft_battleground.repo.repository.MusicListenCountRepo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MusicServiceImpl implements MusicService {
	
	@Autowired
	private MusicListenCountRepo musicListenCountRepo;
	
	@Autowired
	private DumpDataProvider dumpDataProvider;
	
	private Collection<Music> musicList;
	
	private final ReadWriteLock musicReadWriteLock = new ReentrantReadWriteLock();
	private final Queue<MusicEvent> newSongOccurenceQueue = new LinkedList<>();
	private final MusicSongNameMap musicSongNameMap = new MusicSongNameMap();
	private MusicOccurencesCache occurencesCache;
	
	private MusicEvent currentMusicEvent;
	
	@Override
	public Collection<Music> getPlaylist() {
		Collection<Music> musicList;
		Lock musicReadLock = musicReadWriteLock.readLock();
		try {
			musicReadLock.lock();
			musicList = this.musicList;
		} finally {
			musicReadLock.unlock();
		}
		
		return musicList;
	}

	@Override
	public void addOccurence(MusicEvent event) {
		Lock writeLock = this.musicReadWriteLock.writeLock();
		try {
			writeLock.lock();
			if(this.currentMusicEvent == null || !this.currentMusicEvent.equals(this.currentMusicEvent)) {
				this.currentMusicEvent = event;
				this.newSongOccurenceQueue.add(event);
			}
		} finally {
			writeLock.unlock();
		}
		
	}

	@Override
	public void updatePlaylist() throws DumpException {
		log.info("Updating music data");

		Set<Music> dumpMusicData = this.loadMusicDataFromDump();
		Collection<Music> musicList = dumpMusicData.stream().collect(Collectors.toList()).stream().sorted().collect(Collectors.toList());
		for(Music music: musicList) {
			MusicListenCount occurences = this.occurencesCache.getOccurencesIdView().get(music.getId());
			if(occurences != null) {
				music.setOccurences(occurences.getOccurences());
			} else {
				music.setOccurences(0L);
			}
		}
		Lock writeLock = this.musicReadWriteLock.writeLock();
		try {
			writeLock.lock();
			this.musicList = musicList;
		} finally {
			writeLock.unlock();
		}
		log.info("music data update complete");
	}

	@Override
	public void updateOccurences() {
		Lock writeLock = this.musicReadWriteLock.writeLock();
		try {
			writeLock.lock();
			while(!this.newSongOccurenceQueue.isEmpty()) {
				MusicEvent event = this.newSongOccurenceQueue.poll();
				Long id = this.musicSongNameMap.getMusicIdBySong(event.getSongName());
				if(id != null) {
					this.occurencesCache.updateExistingOccurence(id, 1L);
					MusicListenCount mlc= this.occurencesCache.getOccurencesById(id);
					if(mlc == null) {
						mlc = new MusicListenCount(id, event.getSongName());
					}
					this.updateMusicListenCountRepo(mlc);
				} else {
					log.warn("Could not find music id while adding music occurences for song {}", event.getSongName());
				}
			}
		} finally {
			writeLock.unlock();
		}
	}
	
	@Override
	public void freshLoad(Collection<Music> dumpMusicData, List<MusicListenCount> occurrenceData) {
		Lock writeLock = this.musicReadWriteLock.writeLock();
		try {
			writeLock.lock();
			this.occurencesCache.addNewOccurence(occurrenceData);
			this.musicSongNameMap.refreshCache(dumpMusicData);
			for(Music music: dumpMusicData) {
				MusicListenCount occurences = this.occurencesCache.getOccurencesById(music.getId());
				if(occurences != null) {
					music.setOccurences(occurences.getOccurences());
				} else {
					music.setOccurences(0L);
				}
			}
		} finally {
			writeLock.unlock();
		}
	}
	
	@Override
	public Set<Music> loadMusicDataFromDump() throws DumpException {
		Set<Music> musicSet = new HashSet<>();
		String xmlData = this.dumpDataProvider.getMusicXmlString();
		
		try {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(new InputSource(new StringReader(xmlData)));
		doc.getDocumentElement().normalize();
		
		NodeList leafs = doc.getElementsByTagName("leaf");
		for(int i = 0; i < leafs.getLength(); i++) {
			Node node = leafs.item(i);
			if(node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				String name = element.getAttribute("uri");
				name = StringUtils.substringAfterLast(name, "/");
				name = StringUtils.substringBefore(name, ".mp3");
				name = URLDecoder.decode(name, StandardCharsets.UTF_8.toString());
				musicSet.add(new Music(name, element.getAttribute("id"), element.getAttribute("duration")));
			}
		}
		} catch (IOException|SAXException|ParserConfigurationException e) {
			log.error("error updating music data from cache", e);
			throw new DumpException(e, "error updating music data from cache");
		}
		
		return musicSet;
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<MusicListenCount> loadMusicListenCountFromRepo() {
		List<MusicListenCount> list = this.musicListenCountRepo.findAll();
		return list;
	}
	
	@Transactional
	private void updateMusicListenCountRepo(MusicListenCount mlc) {
		this.musicListenCountRepo.saveAndFlush(mlc);
		
		return;
	}

}

@Data
class MusicOccurencesCache {
	private List<MusicListenCount> occurences;
	private Map<Long, MusicListenCount> occurencesIdView;
	private Map<String, MusicListenCount> songNameView;
	
	public MusicOccurencesCache() {
		this.occurences = new ArrayList<>();
		this.occurencesIdView = new HashMap<>();
		this.songNameView = new HashMap<>();
	}
	
	public MusicListenCount getOccurencesById(Long id) {
		MusicListenCount mlc = this.occurencesIdView.get(id);
		return mlc;
	}
	
	public MusicListenCount getOccurencesbySong(String songName) {
		String cleanedSongName = cleanSongNameKey(songName);
		MusicListenCount mlc = this.songNameView.get(cleanedSongName);
		return mlc;
	}
	
	public void addNewOccurence(List<MusicListenCount> list) {
		list.forEach(this::addNewOccurence);
	}
	
	public void addNewOccurence(MusicListenCount mlc) {
		this.occurences.add(mlc);
		this.occurencesIdView.put(mlc.getSongId(), mlc);
		this.songNameView.put(mlc.getSong(), mlc);
	}
	
	public boolean updateExistingOccurence(Long songId, Long additionalOccurences) {
		boolean success = false;
		MusicListenCount mlc = this.occurencesIdView.get(songId);
		if(mlc != null) {
			Long occurences = mlc.getOccurences() + additionalOccurences;
			mlc.setOccurences(occurences);
			success = true;
		}
		
		return success;
	}
	
	private static String cleanSongNameKey(String song) {
		String name = StringUtils.lowerCase(song);
		return name;
	}
}

@Data
class MusicSongNameMap {
	private Map<String, Music> songMusicView;
	
	public MusicSongNameMap() {
		this.songMusicView = new HashMap<>();
	}
	
	public Long getMusicIdBySong(String song) {
		Long id = null;
		String cleanedMusicKey = cleanSongNameKey(song);
		Music music = this.songMusicView.get(cleanedMusicKey);
		if(music != null) {
			id = music.getId();
		}
		return id;
	}
	
	public void refreshCache(Collection<Music> dumpMusicData) {
		for(Music music: dumpMusicData) {
			String cleanedMusicKey = cleanSongNameKey(music);
			if(!this.songMusicView.containsKey(cleanedMusicKey)) {
				this.songMusicView.put(cleanedMusicKey, music);
			}
		}
	}
	
	private static String cleanSongNameKey(Music music) {
		String name = cleanSongNameKey(music.getSongName());
		return name;
	}
	
	private static String cleanSongNameKey(String song) {
		String name = StringUtils.lowerCase(song);
		return name;
	}
}
