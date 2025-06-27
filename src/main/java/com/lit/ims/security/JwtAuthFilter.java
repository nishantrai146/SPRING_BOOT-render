package com.lit.ims.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {
        try {
            String token = jwtService.extractTokenFromRequest(request);

            if (token != null) {
                String username = jwtService.extractUsername(token);
                String role = jwtService.extractRole(token);
                Long companyId = jwtService.extractCompanyId(token);
                Long branchId = jwtService.extractBranchId(token);

                // ✅ Put companyId and branchId in request scope
                request.setAttribute("companyId", companyId);
                request.setAttribute("branchId", branchId);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    var authorities = List.of(new SimpleGrantedAuthority(role));

                    var authToken = new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            authorities
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            chain.doFilter(request, response);
        } catch (Exception e) {
            logger.error("JWT Authentication failed: {}", e.getMessage());
            chain.doFilter(request, response);
        }
    }

}
