package com.tanhua.server.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 替换 Request 对象。
 */
@Component
public class RequestReplaceFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        if (!(httpServletRequest instanceof MyServletRequestWrapper)) {
            httpServletRequest = new MyServletRequestWrapper(httpServletRequest);
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

}
