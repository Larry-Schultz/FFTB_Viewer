package fft_battleground.config;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.Data;

@Data
public class DomainRedirectFilter implements Filter {

    private String destinationDomain = "fftbview.com";
	
    private String sourceServletPath = "theotherbrancomputer.asuscomm.com";

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		String host = request.getServerName();
        String params = StringUtils.replace(host, getSourceServletPath(), "");

        HttpServletResponse httpResponse = (HttpServletResponse) response;
        if(StringUtils.contains(host, destinationDomain)) {
        	httpResponse.setHeader( "Location", getDestinationDomain());
	        httpResponse.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
	    	httpResponse.setHeader( "Connection", "close" );
        } else {
        	chain.doFilter(request, response);
        }
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}
    
}