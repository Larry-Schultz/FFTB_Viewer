package fft_battleground.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import fft_battleground.botland.model.BattleGroundEventType;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.MusicEvent;
import fft_battleground.util.GenericElementOrdering;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WebsocketThread extends Thread {
	
	@Autowired
    private SimpMessagingTemplate template;
	
	@Autowired
	private BlockingQueue<BattleGroundEvent> websocketThreadQueue;
	
	private long currentIndex = 0L; //set to 1 on first ++
	
	protected Map<Long, GenericElementOrdering<BattleGroundEvent>> currentEventIndexMapCache;
	protected MusicEvent currentMusicEvent; //music events should transcend matches and fights
	
	public WebsocketThread() {
		this.currentEventIndexMapCache = new HashMap<>();
		this.setName(this.getClass().getName());
	}
	
	@Override
	public void run() {
		
		while(true) {
			try {
				BattleGroundEvent event = this.websocketThreadQueue.take();
				switch(event.getEventType()) {
					case BETTING_BEGINS: case FIGHT_BEGINS:
						currentIndex++;
						log.debug("Clearing the event cache because of event of type: {}", event.getEventType().toString());
						this.currentEventIndexMapCache = new HashMap<>();
						
						GenericElementOrdering<BattleGroundEvent> element = new GenericElementOrdering<BattleGroundEvent>(currentIndex, event);
						this.currentEventIndexMapCache.put(element.getId(), element);
						//add current music element back into the cache (but no need to send it since live pages will already have the current song set on the page
						//and new pages will get it once they load 
						if(this.currentMusicEvent != null) {
							currentIndex++;
							GenericElementOrdering<BattleGroundEvent> musicEvent = new GenericElementOrdering<BattleGroundEvent>(currentIndex, this.currentMusicEvent);
							this.currentEventIndexMapCache.put(musicEvent.getId(), musicEvent);
						}
						
						this.template.convertAndSend("/matches/events", new GenericElementOrdering<BattleGroundEvent>(currentIndex, event));
						break;
					case LEVEL_UP: case BALANCE: case EXP: case OTHER_PLAYER_BALANCE: case OTHER_PLAYER_EXP:
					case ALLEGIANCE: case PLAYER_SKILL: case BUY_SKILL: case PORTRAIT: case SKILL_WIN: case PRESTIGE_SKILLS:
					case LAST_ACTIVE: case GIFT_SKILL: case PRESTIGE_ASCENSION: case RISER_SKILL_WIN: case BUY_SKILL_RANDOM:
					case CLASS_BONUS: case SKILL_BONUS:
						//do nothing
						break;
					case MUSIC:
						//remove existing music element
						List<Long> musicElements = this.currentEventIndexMapCache.keySet().parallelStream().filter(elementId -> this.currentEventIndexMapCache.get(elementId).getElement().getEventType() == BattleGroundEventType.MUSIC)
							.collect(Collectors.toList());
						musicElements.stream().forEach(elementId -> this.currentEventIndexMapCache.remove(elementId));
						//set the currentMusicEvent to the new MusicEvent object
						MusicEvent newMusicEvent = (MusicEvent) event;
						this.currentMusicEvent = newMusicEvent;
						//store the new music event in the event cache
						currentIndex++;
						GenericElementOrdering<BattleGroundEvent> musicElement = new GenericElementOrdering<BattleGroundEvent>(currentIndex, event);
						this.currentEventIndexMapCache.put(musicElement.getId(), musicElement);
						//send music event to the websocket
						this.template.convertAndSend("/matches/events", musicElement);
						break;
					default:
						currentIndex++;
						GenericElementOrdering<BattleGroundEvent> element2 = new GenericElementOrdering<BattleGroundEvent>(currentIndex, event);
						this.currentEventIndexMapCache.put(element2.getId(), element2);
						this.template.convertAndSend("/matches/events", element2);
						break;
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public List<GenericElementOrdering<BattleGroundEvent>> getCurrentEventCache() {
		return new ArrayList<GenericElementOrdering<BattleGroundEvent>>(this.currentEventIndexMapCache.values());
	}
	
	public GenericElementOrdering<BattleGroundEvent> getEventByOrderId(Long orderId) {
		return this.currentEventIndexMapCache.get(orderId);
	}

}
