package fft_battleground.irc;

import java.io.IOException;

import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gikk.twirk.Twirk;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class IrcChatbotThread extends Thread {

    @Value("${irc.channel}")
    private String channel;
	
    @Autowired
    private Twirk ircChatBot;

    public IrcChatbotThread() {
        super(IrcChatbotThread.class.getName());
    }

    @SneakyThrows
    public void run() {
        log.info("Starting IrcChatbotThread");
        log.info("Joining channel: " + "#" + this.channel);
        this.ircChatBot.connect();
    }

    public void killBot() {
        this.ircChatBot.close();
    }

}