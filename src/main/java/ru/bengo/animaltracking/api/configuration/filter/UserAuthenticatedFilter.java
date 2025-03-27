package ru.bengo.animaltracking.api.configuration.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Enumeration;

@Component
public class UserAuthenticatedFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {

        Enumeration<String> headerNames = request.getHeaderNames();

        String servletPath = request.getServletPath();

        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                if (headerNames.nextElement().equals("authorization") && servletPath.equals("/registration")) {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    return;
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
