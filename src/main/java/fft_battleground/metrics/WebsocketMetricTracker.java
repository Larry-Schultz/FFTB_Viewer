package fft_battleground.metrics;

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WebsocketMetricTracker {

	@Autowired
	private MeterRegistry meterRegistry;
	
	private Gauge currentlyOpenWebsocketSessionsGauge;
	private AtomicInteger currentlyOpenWebsocketSessions = new AtomicInteger();
	
	public WebsocketMetricTracker() { }
	
	@PostConstruct 
	public void setUpMetrics() {
		this.currentlyOpenWebsocketSessionsGauge = Gauge.builder("fft_battleground.openWebsocketSessions", this.currentlyOpenWebsocketSessions, AtomicInteger::get)
				.description("The current count of open websocket sessions in the Viewer")
				.register(this.meterRegistry);
	}
	
	@EventListener
	private void handleSessionConnected(SessionConnectEvent event) {
		this.currentlyOpenWebsocketSessions.incrementAndGet();
	}
	
	@EventListener
	private void handleSessionDisconnect(SessionDisconnectEvent event) {
		this.currentlyOpenWebsocketSessions.decrementAndGet();
	}
}
