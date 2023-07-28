package fft_battleground.dump.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Maps;
import com.google.common.collect.MapDifference.ValueDifference;

import fft_battleground.dump.DumpDataProvider;
import fft_battleground.dump.cache.map.AllegianceCache;
import fft_battleground.dump.cache.map.BalanceCache;
import fft_battleground.dump.cache.map.ClassBonusCache;
import fft_battleground.dump.cache.map.ExpCache;
import fft_battleground.dump.cache.map.LastActiveCache;
import fft_battleground.dump.cache.map.LastFightActiveCache;
import fft_battleground.dump.cache.map.PortraitCache;
import fft_battleground.dump.cache.map.PrestigeSkillsCache;
import fft_battleground.dump.cache.map.SkillBonusCache;
import fft_battleground.dump.cache.map.SnubCache;
import fft_battleground.dump.cache.map.UserSkillsCache;
import fft_battleground.dump.cache.set.BotCache;
import fft_battleground.dump.cache.set.SoftDeleteCache;
import fft_battleground.dump.cache.startup.DumpCacheBuilder;
import fft_battleground.event.detector.model.BalanceEvent;
import fft_battleground.event.detector.model.ExpEvent;
import fft_battleground.event.detector.model.LastActiveEvent;
import fft_battleground.event.detector.model.SnubEvent;
import fft_battleground.event.detector.model.composite.OtherPlayerBalanceEvent;
import fft_battleground.event.detector.model.composite.OtherPlayerExpEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BattleGroundEventType;
import fft_battleground.exception.CacheBuildException;
import fft_battleground.exception.DumpException;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.repo.util.BalanceType;
import fft_battleground.repo.util.BalanceUpdateSource;
import fft_battleground.util.GambleUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DumpCacheManager {

	@Autowired
	private DumpDataProvider dumpDataProvider;
	
	@Autowired
	@Getter @Setter private BalanceCache balanceCache;
	
	@Autowired
	@Getter @Setter private ExpCache expCache;
	
	@Autowired
	@Getter @Setter private LastActiveCache lastActiveCache;
	
	@Autowired
	@Getter @Setter private SnubCache snubCache;
	
	@Autowired
	@Getter @Setter private LastFightActiveCache lastFightActiveCache;
	
	@Autowired
	@Getter @Setter private PortraitCache portraitCache;
	
	@Autowired
	@Getter @Setter private AllegianceCache allegianceCache;
	
	@Autowired
	@Getter @Setter private UserSkillsCache userSkillsCache;
	
	@Autowired
	@Getter @Setter private PrestigeSkillsCache prestigeSkillsCache;
	
	@Autowired
	@Getter @Setter private ClassBonusCache classBonusCache;
	
	@Autowired
	@Getter @Setter private SkillBonusCache skillBonusCache;
	
	@Autowired
	@Getter @Setter private BotCache botCache;
	
	@Autowired
	@Getter @Setter private SoftDeleteCache softDeleteCache;
	
	public Collection<BattleGroundEvent> getBalanceUpdatesFromDumpService() throws DumpException {
		Collection<BattleGroundEvent> data = new LinkedList<BattleGroundEvent>();
		log.info("updating balance cache");
		Map<String, Integer> newBalanceDataFromDump = this.dumpDataProvider.getHighScoreDump();
		Map<String, ValueDifference<Integer>> balanceDelta = Maps.difference(this.balanceCache.getMap(), newBalanceDataFromDump).entriesDiffering();
		OtherPlayerBalanceEvent otherPlayerBalance= new OtherPlayerBalanceEvent(BattleGroundEventType.OTHER_PLAYER_BALANCE, new ArrayList<BalanceEvent>());
		//find differences in balance
		for(String key: balanceDelta.keySet()) {
			BalanceEvent newEvent = new BalanceEvent(key, balanceDelta.get(key).rightValue(), BalanceType.DUMP, BalanceUpdateSource.DUMP);
			otherPlayerBalance.getOtherPlayerBalanceEvents().add(newEvent);
			//update cache with new data
			this.balanceCache.put(key, balanceDelta.get(key).rightValue());
		}
		data.add(otherPlayerBalance);
		
		//find missing players
		for(String key: newBalanceDataFromDump.keySet()) {
			if(!this.balanceCache.containsKey(key)) {
				BalanceEvent newEvent = new BalanceEvent(key, newBalanceDataFromDump.get(key), BalanceType.DUMP, BalanceUpdateSource.DUMP);
				otherPlayerBalance.getOtherPlayerBalanceEvents().add(newEvent);
				this.balanceCache.put(key, newBalanceDataFromDump.get(key));
			}
		}
		log.info("balance cache update complete");
		
		return data;
	}
	
	public Collection<BattleGroundEvent> getExpUpdatesFromDumpService() throws DumpException {
		Collection<BattleGroundEvent> data = new LinkedList<BattleGroundEvent>();
		
		log.info("updating exp cache");
		Map<String, ExpEvent> newExpDataFromDump = this.dumpDataProvider.getHighExpDump();
		Map<String, ValueDifference<ExpEvent>> expDelta = Maps.difference(this.expCache.getMap(), newExpDataFromDump).entriesDiffering();
		OtherPlayerExpEvent expEvents = new OtherPlayerExpEvent(new ArrayList<ExpEvent>());
		//find difference in xp
		for(String key: expDelta.keySet()) {
			ExpEvent newEvent = expDelta.get(key).rightValue();
			expEvents.getExpEvents().add(newEvent);
			this.expCache.put(key, newEvent);
		}
		data.add(expEvents);
		
		//find missing players
		for(String key : newExpDataFromDump.keySet()) {
			if(!this.expCache.containsKey(key)) {
				ExpEvent newEvent = newExpDataFromDump.get(key);
				expEvents.getExpEvents().add(newEvent);
				this.expCache.put(key, newEvent);
			}
		}
		log.info("exp cache update complete");
		
		return data;
	}
	
	public Collection<BattleGroundEvent> getLastActiveUpdatesFromDumpService() throws DumpException {
		Collection<BattleGroundEvent> data = new LinkedList<BattleGroundEvent>();

		log.info("updating last active cache");
		Map<String, Date> newLastActiveFromDump = this.dumpDataProvider.getLastActiveDump();
		Map<String, ValueDifference<Date>> lastActiveDelta = Maps.difference(this.lastActiveCache.getMap(), newLastActiveFromDump).entriesDiffering();
		//find difference in last Active
		for(String key: lastActiveDelta.keySet()) {
			LastActiveEvent newEvent = new LastActiveEvent(key, lastActiveDelta.get(key).rightValue());
			data.add(newEvent);
			//update cache with new data
			this.lastActiveCache.put(key, lastActiveDelta.get(key).rightValue());
		}
		
		//find missing players
		for(String key: newLastActiveFromDump.keySet()) {
			if(!this.lastActiveCache.containsKey(key)) {
				LastActiveEvent newEvent = new LastActiveEvent(key, newLastActiveFromDump.get(key));
				data.add(newEvent);
				this.lastActiveCache.put(key, newLastActiveFromDump.get(key));
			}
		}
		log.info("last active cache update complete");
		
		return data;
	}
	
	public Collection<BattleGroundEvent> getSnubUpdatesFromDumpService() throws DumpException {
		Collection<BattleGroundEvent> data = new LinkedList<BattleGroundEvent>();
		
		log.info("updating snub cache");
		Map<String, Integer> newSnubDataFromDump = this.dumpDataProvider.getSnubData();
		Map<String, ValueDifference<Integer>> snubDelta = Maps.difference(this.snubCache.getMap(), newSnubDataFromDump).entriesDiffering();
		//find difference in snub
		for(String key: snubDelta.keySet()) {
			SnubEvent newEvent = new SnubEvent(key, snubDelta.get(key).rightValue());
			data.add(newEvent);
		}
		
		//find missing players
		for(String key: newSnubDataFromDump.keySet()) {
			if(!this.snubCache.containsKey(key)) {
				SnubEvent newEvent = new SnubEvent(key, newSnubDataFromDump.get(key));
				data.add(newEvent);
				this.snubCache.put(key, newSnubDataFromDump.get(key));
			}
		}
		log.info("snub data update complete");
		
		return data;
	}
	
	public Date getLastActiveDateFromCache(String player) {
		Date date = this.lastActiveCache.get(player);
		return date;
	}
	
	public Integer getBalanceFromCache(String player) {
		Integer balance = this.balanceCache.get(player);
		return balance;
	}
	
	public void updateBalanceCache(BalanceEvent event) {
		this.balanceCache.put(event.getPlayer(), event.getAmount());
	}
}
