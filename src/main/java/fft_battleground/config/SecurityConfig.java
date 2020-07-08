package fft_battleground.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	/*
	 * @Bean public DomainRedirectFilter domainRedirectFilter() {
	 * DomainRedirectFilter filter = new DomainRedirectFilter(); return filter; }
	 * 
	 * @Bean public FilterRegistrationBean<DomainRedirectFilter>
	 * requestDomainRedirectFilter() { final
	 * FilterRegistrationBean<DomainRedirectFilter> reg = new
	 * FilterRegistrationBean<DomainRedirectFilter>(domainRedirectFilter());
	 * reg.addUrlPatterns("/*"); reg.setOrder(1); //defines filter execution order
	 * return reg; }
	 */
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
	    http.csrf().disable();

	    http
        .authorizeRequests()
        .antMatchers("/**").permitAll();
	    
	    http.requiresChannel().anyRequest().requiresSecure();
	}
	
	@Bean
	public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
	    StrictHttpFirewall firewall = new StrictHttpFirewall();
	    firewall.setAllowUrlEncodedSlash(true);    
	    return firewall;
	}
	
}
