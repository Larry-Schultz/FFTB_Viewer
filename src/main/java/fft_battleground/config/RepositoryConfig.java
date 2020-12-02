package fft_battleground.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = {"fft_battleground.repo.model"} )
@EnableJpaRepositories(basePackages = {"fft_battleground.repo.repository"})
public class RepositoryConfig {

}
