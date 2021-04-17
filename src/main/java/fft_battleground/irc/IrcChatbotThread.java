package fft_battleground.irc;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gikk.twirk.Twirk;

import fft_battleground.CoreApplication;
import fft_battleground.discord.WebhookManager;
import fft_battleground.exception.IrcConnectionException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class IrcChatbotThread extends Thread {

    @Value("${irc.channel}")
    private String channel;
    
    @Autowired
	private WebhookManager errorWebhookManager;
    
    @Autowired
    private IrcInitialConnectManager ircInitialConnectManager;
	
    @Autowired
    private Twirk ircChatBot;
    
    @Autowired
    private DisconnectListener disconnectListener;
    
    @Autowired
    private CoreApplication coreApplication;
    
    @PostConstruct
    private void setup() {
    	this.ircChatBot.addIrcListener(this.disconnectListener);
    }

    public IrcChatbotThread() {
        super(IrcChatbotThread.class.getName());
    }

    @SneakyThrows
    public void run() {
        log.info("Starting IrcChatbotThread");
        log.info("Joining channel: " + "#" + this.channel);
        try {
	        boolean startupSuccessful = this.ircInitialConnectManager.connectToIrc(this.ircChatBot);
        } catch(IrcConnectionException e) {
        	log.error(e.getMessage(), e);
        	this.errorWebhookManager.sendShutdownNotice(e, e.getMessage());
        	this.coreApplication.shutdownServer(e);
        }
    }

    public void killBot() {
        this.ircChatBot.close();
    }

}