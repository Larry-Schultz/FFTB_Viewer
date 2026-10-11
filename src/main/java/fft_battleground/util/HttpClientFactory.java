package fft_battleground.util;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate with sane timeouts. A bare new RestTemplate() has no connect or
 * read timeout, so a hung external API (fftbg.com, mustad.io) pins the calling
 * thread forever - which for the scheduled tasks means the whole daily batch.
 */
public final class HttpClientFactory {

	private static final int CONNECT_TIMEOUT_MILLIS = 10_000;
	private static final int READ_TIMEOUT_MILLIS = 30_000;

	private HttpClientFactory() {}

	public static RestTemplate createRestTemplate() {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
		requestFactory.setReadTimeout(READ_TIMEOUT_MILLIS);
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(requestFactory);
		return restTemplate;
	}
}
