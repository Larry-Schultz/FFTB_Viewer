package fft_battleground.scheduled.tasks.daily;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.cache.map.LastActiveCache;
import fft_battleground.dump.cache.map.LastFightActiveCache;
import fft_battleground.scheduled.tasks.DumpDailyScheduledTask;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CheckCertificateDailyTask extends DumpDailyScheduledTask {
	
	@Value("${server.ssl.key-store}")
	private String keyStoreLocation;

	@Value("${server.ssl.key-store-password}")
	private String keyStorePass;

	@Autowired
	private WebhookManager errorWebhookManager;

	public CheckCertificateDailyTask(@Autowired LastActiveCache lastActiveCache,
			@Autowired LastFightActiveCache lastFightActiveCache) {
		super(lastActiveCache, lastFightActiveCache);
	}

	protected void task() {
		this.checkExpirationDateOfCertificate();
	}

	private void checkExpirationDateOfCertificate() {
		try {
			//resolve the configured keystore (classpath:, file:, or plain path) instead of assuming classpath,
			//and load it as a stream - getFile() can never resolve inside an executable war
			Resource keystoreResource = new DefaultResourceLoader().getResource(this.keyStoreLocation);
	        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
	        try (InputStream keystoreStream = keystoreResource.getInputStream()) {
	        	keystore.load(keystoreStream, this.keyStorePass.toCharArray() );
	        }
	        Enumeration<String> aliases = keystore.aliases();
	        for(; aliases.hasMoreElements();) {
	            String alias = (String) aliases.nextElement();
	            X509Certificate cert= ((X509Certificate) keystore.getCertificate(alias));
	            Date certExpiryDate = cert.getNotAfter();
	            Principal subject = cert.getSubjectDN();
	            SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
	            //Tue Oct 17 06:02:22 AEST 2006
	            Date today = new Date();
	            long dateDiff = certExpiryDate.getTime() - today.getTime();
	            Long expiresIn = dateDiff / (24 * 60 * 60 * 1000);
	            if(expiresIn < 7) {
	            	this.errorWebhookManager.sendMessage("The certificate expires in" + expiresIn.toString() + " days!");
	            }
	            log.info("Certifiate: " + alias + "\tExpires On: " + certExpiryDate + "\tFormated Date: " + ft.format(certExpiryDate) + "\tToday's Date: " + ft.format(today) + "\tExpires In: "+ expiresIn);
	        }
		} catch (KeyStoreException|NoSuchAlgorithmException|CertificateException|IOException e) {
			log.error("Error loading certificate", e);
		} 
	}
}