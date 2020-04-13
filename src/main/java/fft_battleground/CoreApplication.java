package fft_battleground;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import fft_battleground.controller.WebsocketThread;
import fft_battleground.event.EventManager;
import fft_battleground.event.EventParser;
import fft_battleground.irc.IrcChatMessenger;
import fft_battleground.irc.IrcChatbotThread;
import fft_battleground.repo.RepoManager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CoreApplication {

	@Autowired
	private IrcChatbotThread ircChatbotThread;
	
	@Autowired
	private EventParser parser; 
	
	@Autowired
	private EventManager eventManager;
	
	@Autowired
	private IrcChatMessenger ircChatMessenger; 
	
	@Autowired
	private RepoManager repoManager; 
	
	@Autowired
	private WebsocketThread websocketThread;
	
	@Value("${fft_battleground.interactive}") 
	private String interactiveMode;
	
	@Value("${useIrc}") 
	private String useIrc;
	
	@EventListener(ApplicationReadyEvent.class)
	@SneakyThrows
	void run() {
		log.error("Starting application");
		
		parser.start();
		eventManager.start();
		repoManager.start();
		websocketThread.start();
		
		if(useIrc.equalsIgnoreCase("true")) {
			ircChatMessenger.start();
			ircChatbotThread.start();
		}
		/*
		 * if(interactiveMode.equals("true")) { while(true) { BufferedReader reader =
		 * new BufferedReader(new InputStreamReader(System.in));
		 * 
		 * // Reading data using readLine String message = reader.readLine();
		 * eventManager.sendMessage(message); } }
		 */
	}
}
