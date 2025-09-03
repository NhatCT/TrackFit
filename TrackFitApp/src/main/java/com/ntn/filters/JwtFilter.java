// com/ntn/filters/JwtFilter.java
package com.ntn.filters;

import com.ntn.utils.JwtUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class JwtFilter implements Filter {

    private boolean isPublic(HttpServletRequest req) {
        String path = req.getRequestURI().substring(req.getContextPath().length());
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) return true;
        return path.startsWith("/api/login") || path.startsWith("/api/register");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest http = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        if (isPublic(http)) {
            chain.doFilter(request, response);
            return;
        }

        String auth = http.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                String username = JwtUtils.validateTokenAndGetUsername(token);
                List<String> roles = JwtUtils.getRoles(token);
                if (username != null) {
                    var authorities = roles.stream()
                            .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
                    var authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT không hợp lệ / hết hạn");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
