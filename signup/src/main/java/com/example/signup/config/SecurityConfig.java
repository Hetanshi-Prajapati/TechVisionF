package com.example.signup.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfFilter;

import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;

@Configuration
public class SecurityConfig {

    @Autowired
    private CustomOAuth2SuccessHandler successHandler;

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    private SessionAuthenticationFilter sessionAuthenticationFilter;

    @Autowired
    private CsrfCookieFilter csrfCookieFilter;

    private OAuth2AuthorizationRequestResolver authorizationRequestResolver() {
        DefaultOAuth2AuthorizationRequestResolver resolver = new
        DefaultOAuth2AuthorizationRequestResolver(
        this.clientRegistrationRepository, "/oauth2/authorization");
        resolver.setAuthorizationRequestCustomizer(customizer -> {
            customizer.additionalParameters(params -> params.put("prompt",
            "select_account"));
        });
        return resolver;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationEntryPoint customEntryPoint() {
        return (request, response, authException) -> {
            boolean isBrowser = GlobalExceptionHandler.isBrowserRequest(request);
            String uri = request.getRequestURI();
            boolean isApiRoute = uri.startsWith("/api/");

            // UI routes should redirect to the login page in the browser.
            boolean isUiRoute = uri.equals("/api/auth/login") || uri.equals("/api/auth/signup")
                    || uri.equals("/api/auth/home") ||
                    uri.equals("/api/auth/explore") || uri.equals("/api/auth/profile") || uri.equals("/api/auth/search")
                    ||
                    uri.equals("/api/auth/settings") || uri.equals("/api/auth/complete-profile")
                    || uri.equals("/api/auth/terms") ||
                    uri.equals("/api/auth/privacy") || uri.equals("/api/auth/about") || uri.equals("/api/auth/contact")
                    ||
                    uri.equals("/api/auth/reset") || uri.equals("/") || uri.equals("/login");

            if (isBrowser) {
                if (isUiRoute) {
                    response.sendRedirect("/api/auth/login");
                } else if (isApiRoute) {
                    response.setStatus(401);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
                } else {
                    response.setStatus(401);
                    response.setContentType("text/html;charset=UTF-8");
                    response.getWriter().write(
                            "<!DOCTYPE html><html><head><title>Please Login</title></head><body style=\"background-color: white; color: #333; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; font-family: sans-serif;\"><h1 style=\"font-weight: normal;\">Please login to continue.</h1></body></html>");
                }
            } else {
                response.setStatus(401);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
            }
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> {
                    CookieCsrfTokenRepository csrfRepo = CookieCsrfTokenRepository.withHttpOnlyFalse();
                    csrfRepo.setCookiePath("/");
                    csrf.csrfTokenRepository(csrfRepo)
                            .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler());
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .addFilterBefore(sessionAuthenticationFilter,
                        org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(csrfCookieFilter, CsrfFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/login",
                                "/oauth2/**",
                                "/images/**",
                                "/css/**",
                                "/js/**",
                                "/img/**",
                                "/uploads/**",
                                "/*.svg",
                                "/*.png")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/auth/login",
                                "/api/auth/signup",
                                "/api/auth/terms",
                                "/api/auth/privacy",
                                "/api/auth/about",
                                "/api/auth/contact",
                                "/api/auth/reset",
                                "/api/auth/remember",
                                "/api/auth/oauth2/start/**",
                                "/api/auth/users/**",
                                "/api/posts/explore",
                                "/api/posts/user/**",
                                "/api/search",
                                "/login/oauth2/**",
                                "/oauth2/authorization/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/login",
                                "/api/auth/signup",
                                "/api/auth/forgot",
                                "/api/auth/reset")
                        .permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/auth/home",
                                "/api/auth/profile",
                                "/api/auth/explore",
                                "/api/auth/search",
                                "/api/auth/settings",
                                "/api/auth/complete-profile")
                        .authenticated()
                        .requestMatchers("/api/auth/logout",
                                "/api/auth/users/me",
                                "/api/auth/users/me/**",
                                "/api/posts/feed",
                                "/api/posts/liked",
                                "/api/posts/likedIds",
                                "/api/posts/**")
                        .authenticated()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customEntryPoint())
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            boolean isBrowser = GlobalExceptionHandler.isBrowserRequest(request);
                            boolean isApiRoute = request.getRequestURI().startsWith("/api/");
                            if (isBrowser && !isApiRoute) {
                                response.setStatus(403);
                                response.setContentType("text/html;charset=UTF-8");
                                response.getWriter().write(
                                        "<!DOCTYPE html><html><head><title>Access Denied</title></head><body style=\"background-color: white; color: #333; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; font-family: sans-serif;\"><h1 style=\"font-weight: normal;\">You do not have permission to access this resource.</h1></body></html>");
                            } else {
                                response.setStatus(403);
                                response.setContentType("application/json");
                                response.getWriter().write(
                                        "{\"error\":\"Access Denied\",\"message\":\"Insufficient permissions\"}");
                            }
                        }))
                .oauth2Login(oauth -> oauth
                        .loginPage("/api/auth/login")
                        .authorizationEndpoint(endpoint -> endpoint
                                .authorizationRequestResolver(authorizationRequestResolver()))
                        .successHandler(successHandler));

        return http.build();
    }
}
