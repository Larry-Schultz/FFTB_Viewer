package fft_battleground.config.interceptors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class DomainHandlerInterceptor implements HandlerInterceptor {

	@Value("${hostnameUrl}")
	private String hostDomain;
	
	@Value("theotherbrancomputer.asuscomm.com")
	private String oldDomain;
	
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
    	String requestDomain = request.getServerName();
    	if(StringUtils.contains(requestDomain, oldDomain)) {
    		String path = request.getRequestURI();
    		String schema = request.getScheme();
    		String newUrl = schema + "://" + path + this.hostDomain;
            response.sendRedirect(newUrl);
            return false;
    	}
    	
		return true;
    }
}