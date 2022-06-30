package fft_battleground.metrics.model;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class AccessEntry {
	private String page;
	private String userAgent;
	private HttpServletRequest request;
	
	private Object urlLStuff;
	
	public String getUrl() throws UnknownHostException {
		InetAddress addr = InetAddress.getByName(request.getRemoteAddr());
		String host = addr.getHostName();
		return host;
	}

	public Object getUrlLStuff() {
		return urlLStuff;
	}

	public void setUrlLStuff(Object urlLStuff) {
		this.urlLStuff = urlLStuff;
	}

	public AccessEntry(String pageName, String userAgent, HttpServletRequest request) {
		this.page = pageName;
		this.userAgent = userAgent;
		this.request = request;
	}

}