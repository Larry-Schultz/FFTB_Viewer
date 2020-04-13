package fft_battleground.event;

import java.util.Timer;
import java.util.TimerTask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.util.MissingEventTypeException;
import fft_battleground.util.Router;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BattleGroundEventBackPropagation {

	public static final long delayIncrement = 500;
	
	@Autowired
	private Router<BattleGroundEvent> eventRouter;
	
	private Timer eventTimer = new Timer();
	
	public BattleGroundEventBackPropagation() {}
	
	public void sendUnitThroughTimer(BattleGroundEvent event, long timerDelay) throws MissingEventTypeException {
		if(event.getEventType() == null) {
			log.error("The event code is missing for event: {}", event);
			throw new MissingEventTypeException("Missing event code for event: " + event.toString());
		}
		this.eventTimer.schedule(new EventSender(this.eventRouter, event), delayIncrement * timerDelay);
	}
	
	public void SendUnitThroughTimer(BattleGroundEvent event) throws MissingEventTypeException {
		if(event.getEventType() == null) {
			log.error("The event code is missing for event: {}", event);
			throw new MissingEventTypeException("Missing event code for event: " + event.toString());
		}
		
		this.eventTimer.schedule(new EventSender(this.eventRouter, event), delayIncrement);
	}
	
}

@Slf4j
class EventSender extends TimerTask {

	private Router<BattleGroundEvent> routerRef;
	private BattleGroundEvent event;
	
	public EventSender() {}
	
	public EventSender(Router<BattleGroundEvent> routerRef, BattleGroundEvent event) {
		this.routerRef = routerRef;
		this.event = event;
	}
	
	@Override
	public void run() {
		log.info("Found event: {} with data: {}", event.getEventType().getEventStringName(), event.toString());
		this.routerRef.sendDataToQueues(event);
	}
	
}