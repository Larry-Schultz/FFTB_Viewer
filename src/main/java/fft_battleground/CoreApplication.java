package fft_battleground;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import fft_battleground.controller.WebsocketThread;
import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.DumpService;
import fft_battleground.dump.cache.startup.DumpCacheBuilder;
import fft_battleground.event.EventManager;
import fft_battleground.event.EventParser;
import fft_battleground.irc.IrcChatMessenger;
import fft_battleground.irc.IrcChatbotThread;
import fft_battleground.metrics.AccessTracker;
import fft_battleground.mustadio.MustadioService;
import fft_battleground.repo.RepoManager;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CoreApplication implements ApplicationContextAware {

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
	
	@Autowired
	private AccessTracker accessTracker;
	
	@Autowired
	private DumpCacheBuilder dumpCacheBuilder;
	
	@Value("${fft_battleground.interactive}") 
	private String interactiveMode;
	
	@Value("${useIrc}") 
	private String useIrc;
	
	@Autowired
	private WebhookManager errorWebhookManager;
	
	@Autowired
	private MustadioService mustadioService;
	
	private ApplicationContext context;
	
	/**
	 * 
	 */
	@EventListener(ApplicationReadyEvent.class)
	@SneakyThrows
	public void run() {
		log.error("Starting application");
		
		if(useIrc.equalsIgnoreCase("true")) {
			ircChatMessenger.start();
			ircChatbotThread.start();
		}
		
		this.dumpCacheBuilder.setUpCaches();
		
		parser.start();
		eventManager.start();
		repoManager.start();
		websocketThread.start();
		accessTracker.start();
		this.mustadioService.refreshMustadioData();
		
		this.errorWebhookManager.sendMessage("Restarting Server");
	}
	
	public void shutdownServer(Exception e) {
		String errorMessageFormat = "Shutting down server, reason %s";
		String errorMessage = String.format(errorMessageFormat, e.getMessage());
		log.error(errorMessage);
		((ConfigurableApplicationContext) context).close();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = applicationContext;
	}
}
