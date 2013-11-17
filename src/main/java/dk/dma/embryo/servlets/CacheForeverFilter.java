package dk.dma.embryo.servlets;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CacheForeverFilter implements Filter {
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse resp = (HttpServletResponse) response;
        resp.setDateHeader("Expires", System.currentTimeMillis() + 3600 * 24 * 365 * 1000L);
        resp.setDateHeader("Last-Modified", 1);
        resp.setHeader("Cache-Control", "public, max-age=" + (3600 * 24 * 365));
        chain.doFilter(request, response);
    }

    public void destroy() {

    }
}
