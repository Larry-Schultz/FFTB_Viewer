package fft_battleground.bot.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import fft_battleground.bot.model.event.BattleGroundEvent;
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
						
						this.template.convertAndSend("/matches/events", new GenericElementOrdering<BattleGroundEvent>(currentIndex, event));
						break;
					case LEVEL_UP: case BALANCE: case EXP: case OTHER_PLAYER_BALANCE: case OTHER_PLAYER_EXP:
					case ALLEGIANCE: case PLAYER_SKILL: case BUY_SKILL: case PORTRAIT: case SKILL_WIN: case PRESTIGE_SKILLS:
					case LAST_ACTIVE: case GIFT_SKILL:
						//do nothing
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
