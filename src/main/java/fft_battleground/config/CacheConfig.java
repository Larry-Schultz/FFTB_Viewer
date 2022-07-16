package fft_battleground.config;

import java.io.IOException;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import redis.embedded.RedisServer;

@EnableCaching
@Configuration
public class CacheConfig {
	
	@Bean
	@Profile("dev")
	public RedisServer redisServer() throws IOException {
		RedisServer redisServer = new RedisServer(6379);
		redisServer.start();
		return redisServer;
	}

}
