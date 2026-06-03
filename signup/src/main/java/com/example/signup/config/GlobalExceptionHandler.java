package com.example.signup.config;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Object handleAll(Exception ex, HttpServletRequest request) throws Exception {
        if (isBrowserRequest(request)) {
            ModelAndView mav = new ModelAndView("error");
            mav.addObject("message", "Something went wrong. Please try again later.");
            return mav;
        } else {
            Map<String, Object> body = new HashMap<>();
            body.put("status", 500);
            body.put("message", "Something went wrong. Please try again later.");
            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public Object handle404(NoHandlerFoundException ex, HttpServletRequest request) throws Exception {
        if (isBrowserRequest(request)) {
            ModelAndView mav = new ModelAndView("error");
            mav.addObject("message", "The page you are looking for does not exist.");
            return mav;
        } else {
            Map<String, Object> body = new HashMap<>();
            body.put("status", 404);
            body.put("message", "API endpoint not found");
            return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
        }
    }

    public static boolean isBrowserRequest(HttpServletRequest request) {
        String acceptHeader = request.getHeader("Accept");
        if (acceptHeader != null) {
            // If explicit JSON request, it's not a browser page navigation
            if (acceptHeader.contains("application/json")) {
                return false;
            }
            // Browsers prioritize text/html
            if (acceptHeader.contains("text/html")) {
                return true;
            }
        }
        // Default to JSON API behavior for fetch defaults (*/*)
        return false;
    }
}