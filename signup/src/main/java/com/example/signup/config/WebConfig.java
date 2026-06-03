package com.example.signup.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get("signup","uploads");
        String uploadPath = uploadDir.toFile().getAbsolutePath();
        //String uploadPath = uploadDir.toAbsolutePath().toString();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");

        // Explicitly map static resources to prevent breakage when add-mappings=false
        registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/");
        registry.addResourceHandler("/images/**").addResourceLocations("classpath:/static/images/");
        registry.addResourceHandler("/img/**").addResourceLocations("classpath:/static/img/");
    }

    @Override
    public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
        registry.addInterceptor(new org.springframework.web.servlet.HandlerInterceptor() {
            @Override
            public boolean preHandle(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, Object handler) throws Exception {
                String uri = request.getRequestURI();
                if (uri.startsWith("/api/")) {
                    boolean isUiRoute = uri.equals("/api/auth/login") || uri.equals("/api/auth/signup") || uri.equals("/api/auth/home") ||
                        uri.equals("/api/auth/explore") || uri.equals("/api/auth/profile") || uri.equals("/api/auth/search") ||
                        uri.equals("/api/auth/settings") || uri.equals("/api/auth/complete-profile") || uri.equals("/api/auth/terms") ||
                        uri.equals("/api/auth/privacy") || uri.equals("/api/auth/about") || uri.equals("/api/auth/contact") ||
                        uri.equals("/api/auth/reset") || uri.startsWith("/api/auth/oauth2/start") || uri.equals("/") || uri.equals("/login");

                    if (!isUiRoute && GlobalExceptionHandler.isBrowserRequest(request)) {
                        response.setStatus(403);
                        response.setContentType("text/html;charset=UTF-8");
                        response.getWriter().write("<!DOCTYPE html><html><head><title>Access Denied</title></head><body style=\"background-color: white; color: #333; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; font-family: sans-serif;\"><h1 style=\"font-weight: normal;\">Oops! This content cannot be viewed directly.</h1></body></html>");
                        return false;
                    }
                }
                return true;
            }
        });
    }
}
