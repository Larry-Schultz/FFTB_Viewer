package fft_battleground.irc;

import java.io.IOException;

import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class IrcChatbotThread extends Thread {

    @Value("${irc.channel}")
    private String channel;
	
    @Autowired
    private PircBotX ircChatBot;

    public IrcChatbotThread() {
        super(IrcChatbotThread.class.getName());
    }

    public void run() {
        log.info("Starting IrcChatbotThread");
        log.info("Joining channel: " + "#" + this.channel);
        try {
            ircChatBot.startBot();
        } catch (IOException | IrcException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void killBot() {
        this.ircChatBot.close();
    }

}