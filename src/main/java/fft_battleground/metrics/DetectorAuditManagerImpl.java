package fft_battleground.metrics;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import fft_battleground.event.model.BattleGroundEventType;
import fft_battleground.repo.model.DetectorEventAudit;
import fft_battleground.repo.repository.DetectorEventAuditRepo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DetectorAuditManagerImpl implements DetectorAuditManager {

	@Autowired
	private DetectorEventAuditRepo detectorEventAuditRepo;
	
	private final ReadWriteLock cacheLock = new ReentrantReadWriteLock();
	private final Map<BattleGroundEventType, Integer> eventTypeCountCache  = new ConcurrentHashMap<>(); 
	
	@Override
	public void addEvent(BattleGroundEventType type) {
		Lock readLock = this.cacheLock.readLock();
		readLock.lock();
		try {
			if(this.eventTypeCountCache.containsKey(type)) {
				Integer currentValue = this.eventTypeCountCache.get(type);
				this.eventTypeCountCache.put(type, currentValue + 1);
			} else {
				this.eventTypeCountCache.put(type, 1);
			}
		} finally {
			readLock.unlock();
		}
		
	}

	@Override
	public void updateDatabaseAndClearCache() {
		Lock writeLock = this.cacheLock.writeLock();
		writeLock.lock();
		try {
			for(Entry<BattleGroundEventType, Integer> entry: this.eventTypeCountCache.entrySet()) {
				try {
					this.updateDetectorAuditTable(entry.getKey(), entry.getValue());
				} catch(Exception e) {
					log.error("Error updating detector audit table for type {}", entry.getKey(), e);
				}
			}
			this.eventTypeCountCache.clear();
		} finally {
			writeLock.unlock();
		}
	}
	
	@Transactional
	private void updateDetectorAuditTable(BattleGroundEventType type, Integer occurence) {
		Optional<DetectorEventAudit> maybeDetectorEventAudit = this.detectorEventAuditRepo.findById(type);
		if(maybeDetectorEventAudit.isPresent()) {
			Long currentOccurences = maybeDetectorEventAudit.get().getOccurences();
			Long newOccurences = currentOccurences + occurence.longValue();
			maybeDetectorEventAudit.get().setOccurences(newOccurences.longValue());
			this.detectorEventAuditRepo.saveAndFlush(maybeDetectorEventAudit.get());
		} else {
			DetectorEventAudit newDetectorEventAudit = new DetectorEventAudit(type, occurence);
			this.detectorEventAuditRepo.saveAndFlush(newDetectorEventAudit);
		}
		
		return;
	}

}
