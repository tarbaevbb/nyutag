package ru.nyutag.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nyutag.telegram.TelegramUser;
import ru.nyutag.user.User;
import ru.nyutag.user.UserRepository;

import java.time.Instant;

@Service
public class UserService {
    private final UserRepository users;

    public UserService(UserRepository users) {
        this.users = users;
    }

    @Transactional
    public User upsertFromTelegram(TelegramUser tg) {
        User user = users.findByTelegramId(tg.getId()).orElseGet(User::new);
        user.setTelegramId(tg.getId());
        user.setFirstName(tg.getFirstName());
        user.setLastName(tg.getLastName());
        user.setUsername(tg.getUsername());
        if (tg.getPhotoUrl() != null) {
            user.setAvatarUrl(tg.getPhotoUrl());
        }
        user.setUpdatedAt(Instant.now());
        return users.save(user);
    }

    @Transactional
    public User getOrCreateDevUser(long telegramId) {
        return users.findByTelegramId(telegramId).orElseGet(() -> {
            User user = new User();
            user.setTelegramId(telegramId);
            user.setFirstName("Dev");
            user.setLastName("User");
            user.setUsername("dev");
            user.setUpdatedAt(Instant.now());
            return users.save(user);
        });
    }
}