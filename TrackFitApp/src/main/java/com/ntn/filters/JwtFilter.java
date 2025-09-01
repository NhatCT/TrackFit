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

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // Chỉ check các URL bảo vệ /api/secure/**
        if (httpRequest.getRequestURI().startsWith(httpRequest.getContextPath() + "/api/secure")) {
            String header = httpRequest.getHeader("Authorization");

            if (header == null || !header.startsWith("Bearer ")) {
                ((HttpServletResponse) response)
                        .sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header.");
                return;
            }

            String token = header.substring(7);
            try {
                String username = JwtUtils.validateTokenAndGetUsername(token);
                List<String> roles = JwtUtils.getRoles(token);

                if (username != null) {
                    var authorities = roles.stream()
                            .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r) // map -> ROLE_*
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    chain.doFilter(request, response);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            ((HttpServletResponse) response)
                    .sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token không hợp lệ hoặc hết hạn");
            return;
        }

        chain.doFilter(request, response);
    }
}
