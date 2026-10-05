package ru.nyutag.telegram;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class TelegramUser {
    private final Long id;
    private final String firstName;
    private final String lastName;
    private final String username;
    private final String photoUrl;

    public TelegramUser(Long id, String firstName, String lastName, String username, String photoUrl) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.photoUrl = photoUrl;
    }

    public static TelegramUser fromJson(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<?, ?> map = mapper.readValue(json, Map.class);
            Object idObj = map.get("id");
            if (idObj == null) throw new IllegalArgumentException("Missing user id");
            Long id = ((Number) idObj).longValue();
            String first = stringOrNull(map.get("first_name"));
            String last = stringOrNull(map.get("last_name"));
            String username = stringOrNull(map.get("username"));
            String photo = stringOrNull(map.get("photo_url"));
            return new TelegramUser(id, first, last, username, photo);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid user payload", e);
        }
    }

    private static String stringOrNull(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getUsername() { return username; }
    public String getPhotoUrl() { return photoUrl; }
}