package fft_battleground.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.gikk.twirk.Twirk;
import com.gikk.twirk.TwirkBuilder;
import com.gikk.twirk.events.TwirkListener;

import fft_battleground.event.detector.AllegianceDetector;
import fft_battleground.event.detector.BadBetDetector;
import fft_battleground.event.detector.BalanceDetector;
import fft_battleground.event.detector.BetDetector;
import fft_battleground.event.detector.BetInfoEventDetector;
import fft_battleground.event.detector.BettingBeginsDetector;
import fft_battleground.event.detector.BettingEndsDetector;
import fft_battleground.event.detector.BuySkillDetector;
import fft_battleground.event.detector.EventDetector;
import fft_battleground.event.detector.FightDetector;
import fft_battleground.event.detector.GiftSkillDetector;
import fft_battleground.event.detector.LevelUpDetector;
import fft_battleground.event.detector.MatchInfoDetector;
import fft_battleground.event.detector.MusicDetector;
import fft_battleground.event.detector.OtherPlayerBalanceDetector;
import fft_battleground.event.detector.OtherPlayerExpDetector;
import fft_battleground.event.detector.PlayerSkillDetector;
import fft_battleground.event.detector.PortraitEventDetector;
import fft_battleground.event.detector.PrestigeAscensionDetector;
import fft_battleground.event.detector.ResultEventDetector;
import fft_battleground.event.detector.RiserSkillWinDetector;
import fft_battleground.event.detector.SkillDropDetector;
import fft_battleground.event.detector.SkillWinEventDetector;
import fft_battleground.irc.TwirkChatListenerAdapter;
import fft_battleground.model.ChatMessage;
import fft_battleground.model.Images;
import fft_battleground.util.Router;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@EnableJpaRepositories("fft_battleground.repo")
@Configuration
@EnableCaching
@EnableScheduling
@Slf4j
public class Config {


	@Bean
	@SneakyThrows
    public Twirk ircChatBot(@Value("${irc.username}") String username, @Value("${irc.password}") String password, @Value("${irc.channel}") String channel, 
    		Router<ChatMessage> chatMessageRouter) {
		
		final Twirk twirk = new TwirkBuilder("#" +channel, username, password)
								.build();				//Create the Twirk object
		
		twirk.addIrcListener(new TwirkChatListenerAdapter(chatMessageRouter, channel));
		twirk.addIrcListener( new TwirkListener() {
			@Override
			public void onDisconnect() {
				//Twitch might sometimes disconnects us from chat. If so, try to reconnect. 
				try { 
					if( !twirk.connect() )
						//Reconnecting might fail, for some reason. If so, close the connection and release resources.
						twirk.close();
				} 
				catch (IOException e) { 
					//If reconnection threw an IO exception, close the connection and release resources.
					twirk.close(); 
				} 
				catch (InterruptedException e) {  }
			}
		});
		
		
		return twirk;
	}
	
	@Bean
	public Images images() {
		log.debug("loading images");
		Resource imagesJsonFileResource = new ClassPathResource("/images.json");
		String portraitLocation = "/static/img/portraits/";
		String browserBaseUrl = "/img/portraits/";
		Images images = new Images(imagesJsonFileResource, portraitLocation, browserBaseUrl);
		
		return images;
	}
	
	@Bean
	public List<EventDetector> detectors(@Value("${irc.username}") String username) {
		return Arrays.asList(new EventDetector[]{
			new BetDetector(), new LevelUpDetector(), new ResultEventDetector(), new SkillWinEventDetector(), new RiserSkillWinDetector(),
			new BalanceDetector(username), new OtherPlayerBalanceDetector(), new PlayerSkillDetector(), new MusicDetector(),
			new BettingBeginsDetector(), new AllegianceDetector(), new BetInfoEventDetector(), new SkillDropDetector(),
			new BettingEndsDetector(), new BadBetDetector(), new BuySkillDetector(), new PortraitEventDetector(),
			new FightDetector(), new OtherPlayerExpDetector(), new GiftSkillDetector(), new PrestigeAscensionDetector()});
	}
}
