package fft_battleground.config;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fft_battleground.botland.model.BetResults;
import fft_battleground.event.detector.model.BattleGroundEvent;
import fft_battleground.event.model.DatabaseResultsData;
import fft_battleground.model.ChatMessage;
import fft_battleground.util.Router;

@Configuration
public class QueueRouterConfig {

	@Bean
	public BlockingQueue<ChatMessage> eventParserMessageQueue() {
		return new LinkedBlockingQueue<ChatMessage>();
	}
	
	@Bean
    public Router<ChatMessage> chatMessageRouter(Queue<ChatMessage> eventParserMessageQueue) {
        return new Router<ChatMessage>(eventParserMessageQueue);
    }
	
	@Bean
	public BlockingQueue<BattleGroundEvent> eventManagerQueue() {
		return new LinkedBlockingQueue<BattleGroundEvent>();
	}
	
	@Bean
	public BlockingQueue<BattleGroundEvent> websocketThreadQueue() {
		return new LinkedBlockingQueue<BattleGroundEvent>();
	}
	
	@Bean
	public Router<BattleGroundEvent> eventRouter(Queue<BattleGroundEvent> eventManagerQueue, Queue<BattleGroundEvent> websocketThreadQueue) {
		return new Router<BattleGroundEvent>(eventManagerQueue, websocketThreadQueue);
	}
	
	@Bean
	public BlockingQueue<ChatMessage> ircChatMessengerQueue() {
		return new LinkedBlockingQueue<ChatMessage>();
	}
	
	@Bean 
	public Router<ChatMessage> messageSenderRouter(Queue<ChatMessage> ircChatMessengerQueue) {
		return new Router<ChatMessage>(ircChatMessengerQueue);
	}
	
	@Bean
	public BlockingQueue<DatabaseResultsData> betResultsQueue() {
		return new LinkedBlockingQueue<DatabaseResultsData>();
	}
	
	@Bean
	public Router<DatabaseResultsData> betResultsRouter(Queue<DatabaseResultsData> betResultsQueue) {
		return new Router<DatabaseResultsData>(betResultsQueue);
	}
}
