package fft_battleground.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@EntityScan(basePackages = {"fft_battleground.repo.model"} )
@EnableJpaRepositories(basePackages = {"fft_battleground.repo.repository"})
public class RepositoryConfig {

	/*
	 * @Bean public CacheManager cacheManager(Caffeine caffeine) {
	 * CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
	 * caffeineCacheManager.setCaffeine(caffeine); return caffeineCacheManager; }
	 */
}
