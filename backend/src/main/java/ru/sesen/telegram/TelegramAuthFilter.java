package ru.sesen.telegram;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import ru.sesen.common.UnauthorizedException;
import ru.sesen.config.SesenProperties;
import ru.sesen.user.User;
import ru.sesen.user.UserContext;
import ru.sesen.service.TelegramAuthService;
import ru.sesen.service.UserService;

import java.io.IOException;

@Component
public class TelegramAuthFilter implements Filter {
    private final TelegramAuthService telegramAuthService;
    private final UserService userService;
    private final SesenProperties properties;

    public TelegramAuthFilter(TelegramAuthService telegramAuthService, UserService userService,
                              SesenProperties properties) {
        this.telegramAuthService = telegramAuthService;
        this.userService = userService;
        this.properties = properties;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        try {
            String initData = httpRequest.getHeader("X-Telegram-Init-Data");
            User user;

            if (initData != null && !initData.isBlank()) {
                user = telegramAuthService.authenticate(initData);
            } else if (properties.getDevAuth().isEnabled()) {
                user = telegramAuthService.authenticateDev();
                if (user == null) {
                    sendUnauthorized(httpResponse, "Dev auth is not enabled");
                    return;
                }
            } else {
                sendUnauthorized(httpResponse, "Missing authentication");
                return;
            }

            UserContext.set(user);
            filterChain.doFilter(httpRequest, httpResponse);
        } catch (UnauthorizedException e) {
            sendUnauthorized(httpResponse, "Authentication failed");
        } finally {
            UserContext.clear();
        }
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(401);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}