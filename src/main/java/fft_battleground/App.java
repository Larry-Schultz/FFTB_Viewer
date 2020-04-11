package fft_battleground;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

import fft_battleground.bot.EventManager;
import fft_battleground.bot.EventParser;
import fft_battleground.bot.controller.WebsocketThread;
import fft_battleground.irc.IrcChatMessenger;
import fft_battleground.irc.IrcChatbotThread;
import fft_battleground.repo.RepoManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableEncryptableProperties
@SpringBootApplication
public class App extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

	/*
	 * @Bean public CommandLineRunner demo(IrcChatbotThread ircChatbotThread,
	 * EventParser parser, EventManager eventManager, IrcChatMessenger
	 * ircChatMessenger, RepoManager repoManager, WebsocketThread websocketThread,
	 * 
	 * @Value("${fft_battleground.interactive}") String interactiveMode) { return
	 * (args) -> { ircChatbotThread.start(); parser.start(); eventManager.start();
	 * ircChatMessenger.start(); repoManager.start(); websocketThread.start();
	 * if(interactiveMode.equals("true")) { while(true) { BufferedReader reader =
	 * new BufferedReader(new InputStreamReader(System.in));
	 * 
	 * // Reading data using readLine String message = reader.readLine();
	 * eventManager.sendMessage(message); } } }; }
	 */
}
