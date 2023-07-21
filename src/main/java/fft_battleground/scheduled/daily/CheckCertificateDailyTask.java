package fft_battleground.scheduled.daily;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.DumpScheduledTasksManagerImpl;
import fft_battleground.dump.DumpService;
import fft_battleground.scheduled.ScheduledTask;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CheckCertificateDailyTask extends ScheduledTask {
	private String keyStorePassRef;
	private WebhookManager errorWebhookManagerRef;
	
	public CheckCertificateDailyTask(DumpScheduledTasksManagerImpl dumpScheduledTasks, DumpService dumpService) {
		super(dumpScheduledTasks, dumpService);
		this.keyStorePassRef = dumpScheduledTasks.getKeyStorePass();
		this.errorWebhookManagerRef = dumpScheduledTasks.getErrorWebhookManager();
	}
	
	protected void task() {
		this.checkExpirationDateOfCertificate();
	}
	
	private void checkExpirationDateOfCertificate() {
		try {
			Resource keystoreResource = new ClassPathResource("keystore.p12");
	        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
	        keystore.load(new FileInputStream(keystoreResource.getFile()), this.keyStorePassRef.toCharArray() );
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
	            	this.errorWebhookManagerRef.sendMessage("The certificate expires in" + expiresIn.toString() + " days!");
	            }
	            log.info("Certifiate: " + alias + "\tExpires On: " + certExpiryDate + "\tFormated Date: " + ft.format(certExpiryDate) + "\tToday's Date: " + ft.format(today) + "\tExpires In: "+ expiresIn);
	        }
		} catch (KeyStoreException|NoSuchAlgorithmException|CertificateException|IOException e) {
			log.error("Error loading certificate", e);
		} 
	}
}