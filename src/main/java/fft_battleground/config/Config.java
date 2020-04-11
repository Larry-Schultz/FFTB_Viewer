package fft_battleground.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.pircbotx.PircBotX;
import org.pircbotx.cap.EnableCapHandler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.github.benmanes.caffeine.cache.Caffeine;

import fft_battleground.bot.detector.AllegianceDetector;
import fft_battleground.bot.detector.BadBetDetector;
import fft_battleground.bot.detector.BalanceDetector;
import fft_battleground.bot.detector.BetDetector;
import fft_battleground.bot.detector.BetInfoEventDetector;
import fft_battleground.bot.detector.BettingBeginsDetector;
import fft_battleground.bot.detector.BettingEndsDetector;
import fft_battleground.bot.detector.BuySkillDetector;
import fft_battleground.bot.detector.EventDetector;
import fft_battleground.bot.detector.FightDetector;
import fft_battleground.bot.detector.GiftSkillDetector;
import fft_battleground.bot.detector.LevelUpDetector;
import fft_battleground.bot.detector.MatchInfoDetector;
import fft_battleground.bot.detector.OtherPlayerBalanceDetector;
import fft_battleground.bot.detector.OtherPlayerExpDetector;
import fft_battleground.bot.detector.PlayerSkillDetector;
import fft_battleground.bot.detector.PortraitEventDetector;
import fft_battleground.bot.detector.ResultEventDetector;
import fft_battleground.bot.detector.SkillWinEventDetector;
import fft_battleground.dump.Music;
import fft_battleground.irc.TwitchChatListenerAdapter;
import fft_battleground.model.ChatMessage;
import fft_battleground.model.Images;
import fft_battleground.tournament.Tips;
import fft_battleground.tournament.TournamentService;
import fft_battleground.util.Router;
import lombok.extern.slf4j.Slf4j;

@EnableJpaRepositories("fft_battleground.repo")
@Configuration
@EnableCaching
@EnableScheduling
@Slf4j
public class Config {


	@Bean
    public PircBotX ircChatBot(@Value("${irc.username}") String username, @Value("${irc.password}") String password, @Value("${irc.channel}") String channel, 
    		Router<ChatMessage> chatMessageRouter) {
    	PircBotX ircChatBot;
    	// Configure what we want our bot to do
    	org.pircbotx.Configuration configuration = new org.pircbotx.Configuration.Builder().setAutoNickChange(false) // Twitch doesn't support
                                                                                           // multiple users
                .setOnJoinWhoEnabled(false) // Twitch doesn't support WHO command
                .setCapEnabled(true).addCapHandler(new EnableCapHandler("twitch.tv/membership")) // Twitch by default
                                                                                                 // doesn't send JOIN,
                                                                                                 // PART, and NAMES
                                                                                                 // unless you request
                                                                                                 // it, see
                                                                                                 // https://github.com/justintv/Twitch-API/blob/master/IRC.md#membership
                .addServer("irc.twitch.tv")
                .setName(username) // Your twitch.tv username
                .setServerPassword(password) // Your oauth password from http://twitchapps.com/tmi
                .addAutoJoinChannel("#" + channel) // Some twitch channel
                 .addListener(new TwitchChatListenerAdapter(chatMessageRouter, channel)) // Add our listener that will be
                                                                                        // called on Events
                .buildConfiguration();

        // Create our bot with the configuration
        ircChatBot = new PircBotX(configuration);
        
        return ircChatBot;
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
			new BetDetector(), new LevelUpDetector(), new ResultEventDetector(), new SkillWinEventDetector(),
			new BalanceDetector(username), new OtherPlayerBalanceDetector(), new PlayerSkillDetector(),
			new BettingBeginsDetector(), new MatchInfoDetector(), new AllegianceDetector(), new BetInfoEventDetector(),
			new BettingEndsDetector(), new BadBetDetector(), new BuySkillDetector(), new PortraitEventDetector(),
			new FightDetector(), new OtherPlayerExpDetector(), new GiftSkillDetector()});
	}
	
}
